package com.yeetor.androidcontrol;

import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.neovisionaries.ws.client.*;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by harry on 2017/5/3.
 */
public class RemoteClient extends WebSocketAdapter implements MinicapListener, MinitouchListener {

    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<LocalClient.ImageData> dataQueue = new LinkedBlockingQueue<LocalClient.ImageData>();

    private String ip;
    private int port;
    private String key;
    private String serialNumber;
    private WebSocket ws;

    Minicap minicap = null;
    Minitouch minitouch = null;

    public RemoteClient(String ip, int port, String key, String serialNumber) throws IOException, WebSocketException {
        this.ip = ip;
        this.port = port;
        this.key = key;
        this.serialNumber = serialNumber;
        if (serialNumber == null || serialNumber.isEmpty()) {
            IDevice device = AdbServer.server().getFirstDevice();
            if (device == null)
                throw new RuntimeException("未找到设备！");
            this.serialNumber = device.getSerialNumber();
        }

        ws = new WebSocketFactory().createSocket("ws://" + ip + ":" + port);
        ws.addListener(this);
        ws.connect();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        System.out.println("Connect to server " + ip + ":" + port);
        JSONObject obj = new JSONObject();
        obj.put("sn", serialNumber);
        obj.put("key", key);
        websocket.sendText("open://" + obj.toJSONString());
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Command command = Command.ParseCommand(text);
        if (command != null) {
            switch (command.getSchem()) {
                case START:
                case WAITTING:
                case TOUCH:
                case KEYEVENT:
                case INPUT:
                    executeCommand(command);
                    break;
            }
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        System.out.println("Server disconnected");
        System.exit(0);
    }

    @Override
    public void onStartup(Minicap minicap, boolean success) {
        if (ws != null) {
            ws.sendText("minicap://open");
        }
    }

    @Override
    public void onClose(Minicap minicap) {
        if (ws != null) {
            ws.sendText("minicap://close");
        }
    }

    @Override
    public void onBanner(Minicap minicap, Banner banner) {}

    @Override
    public void onJPG(Minicap minicap, byte[] data) {
        if (isWaitting) {
            if (dataQueue.size() > 0) {
                dataQueue.add(new LocalClient.ImageData(data));
                // 挑选没有超时的图片
                LocalClient.ImageData d = getUsefulImage();
                sendImage(d.data);
            } else {
                sendImage(data);
            }
            isWaitting = false;
        } else {
            clearObsoleteImage();
            dataQueue.add(new LocalClient.ImageData(data));
        }
    }

    @Override
    public void onStartup(Minitouch minitouch, boolean success) {
        if (ws != null) {
            ws.sendText("minitouch://open");
        }
    }

    @Override
    public void onClose(Minitouch minitouch) {
        if (ws != null) {
            ws.sendText("minitouch://close");
        }
    }

    private void sendImage(byte[] data) {
        if (ws != null) {
            ws.sendBinary(data);
        }
    }

    private void clearObsoleteImage() {
        LocalClient.ImageData d = dataQueue.peek();
        long curTS = System.currentTimeMillis();
        while (d != null) {
            if (curTS - d.timesp < DATA_TIMEOUT) {
                dataQueue.poll();
                d = dataQueue.peek();
            } else {
                break;
            }
        }
    }

    private LocalClient.ImageData getUsefulImage() {
        long curTS = System.currentTimeMillis();
        // 挑选没有超时的图片
        LocalClient.ImageData d = null;
        while (true) {
            d = dataQueue.poll();
            // 如果没有超时，或者超时了但是最后一张图片，也发送给客户端
            if (d == null || curTS - d.timesp < DATA_TIMEOUT || dataQueue.size() == 0) {
                break;
            }
        }
        return d;
    }

    public void setWaitting(boolean waitting) {
        isWaitting = waitting;
        trySendImage();
    }

    private void trySendImage() {
        LocalClient.ImageData d = getUsefulImage();
        if (d != null) {
            isWaitting = false;
            sendImage(d.data);
        }
    }

    void executeCommand(Command command) {
        switch (command.getSchem()) {
            case START:
                startCommand(command);
                break;
            case TOUCH:
                touchCommand(command);
            case WAITTING:
                waittingCommand(command);
                break;
            case KEYEVENT:
                keyeventCommand(command);
                break;
            case INPUT:
                inputCommand(command);
                break;
        }
    }

    private void startCommand(Command command) {
        String str = command.getString("type", null);
        if (str != null) {
            if (str.equals("minicap")) {
                startMinicap(command);
            } else if (str.equals("minitouch")) {
                startMinitouch(command);
            }
        }
    }

    private void waittingCommand(Command command) {
        setWaitting(true);
    }

    private void keyeventCommand(Command command) {
        int k = Integer.parseInt(command.getContent());
        if (minitouch != null) minitouch.sendKeyEvent(k);
    }


    private void touchCommand( Command command) {
        String str = (String) command.getContent();
        if (minitouch != null) minitouch.sendEvent(str);
    }

    private void inputCommand(Command command) {
        String str = (String) command.getContent();
        if (minitouch != null) minitouch.inputText(str);
    }

    private void startMinicap(Command command) {
        if (minicap != null) {
            minicap.kill();
        }
        // 获取请求的配置
        JSONObject obj = (JSONObject) command.get("config");
        Float scale = obj.getFloat("scale");
        Float rotate = obj.getFloat("rotate");
        if (scale == null) {scale = 0.3f;}
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        if (rotate == null) { rotate = 0.0f; }
        Minicap minicap = new Minicap(serialNumber);
        minicap.addEventListener(this);
        minicap.start(scale, rotate.intValue());
        this.minicap = minicap;
    }

    private void startMinitouch(Command command) {
        if (minitouch != null) {
            minicap.kill();
        }

        Minitouch minitouch = new Minitouch(serialNumber);
        minitouch.addEventListener(this);
        minitouch.start();
        this.minitouch = minitouch;
    }
}
