package com.yeetor.p2p;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;
import com.yeetor.p2p.Protocol;
import com.yeetor.p2p.WSServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import javafx.scene.image.Image;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by harry on 2017/4/19.
 */
public class LocalClient implements MinicapListener, MinitouchListener {
    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<ImageData> dataQueue = new LinkedBlockingQueue<ImageData>();

    private Protocol protocol;

    public LocalClient(Protocol protocol) {
        this.protocol = protocol;
    }


    public void executeCommand(ChannelHandlerContext ctx, Command command) {
        switch (command.getSchem()) {
            case START:
                startCommand(ctx, command);
                break;
            case TOUCH:
                touchCommand(ctx, command);
            case WATTING:
                waittingCommand(ctx, command);
                break;
            case KEYEVENT:
                keyeventCommand(command);
                break;
            case INPUT:
                inputCommand(command);
                break;
        }
    }

    // minicap启动完毕后
    @Override
    public void onStartup(Minicap minicap, boolean success) {
        if (protocol != null && protocol.getBroswerSocket() != null && success) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minicap://open"));
        }
    }

    @Override
    public void onClose(Minicap minicap) {
        if (protocol != null && protocol.getBroswerSocket() != null) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minicap://close"));
        }
    }

    // banner信息读取完毕
    @Override
    public void onBanner(Minicap minicap, Banner banner) {}

    // 读取到图片信息
    @Override
    public void onJPG(Minicap minicap, byte[] data) {
        if (isWaitting) {
            if (dataQueue.size() > 0) {
                dataQueue.add(new ImageData(data));
                // 挑选没有超时的图片
                ImageData d = getUsefulImage();
                sendImage(d.data);
            } else {
                sendImage(data);
            }
            isWaitting = false;
        } else {
            clearObsoleteImage();
            dataQueue.add(new ImageData(data));
        }
    }

    @Override
    public void onStartup(Minitouch minitouch, boolean success) {
        if (protocol != null && protocol.getBroswerSocket() != null && success) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minitouch://open"));
        }
    }

    @Override
    public void onClose(Minitouch minitouch) {
        if (protocol != null && protocol.getBroswerSocket() != null) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minitouch://close"));
        }
    }

    public void setWaitting(boolean waitting) {
        isWaitting = waitting;
        trySendImage();
    }

    private void trySendImage() {
        ImageData d = getUsefulImage();
        if (d != null) {
            isWaitting = false;
            sendImage(d.data);
        }
    }

    private void clearObsoleteImage() {
        ImageData d = dataQueue.peek();
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

    private ImageData getUsefulImage() {
        long curTS = System.currentTimeMillis();
        // 挑选没有超时的图片
        ImageData d = null;
        while (true) {
            d = dataQueue.poll();
            // 如果没有超时，或者超时了但是最后一张图片，也发送给客户端
            if (d == null || curTS - d.timesp < DATA_TIMEOUT || dataQueue.size() == 0) {
                break;
            }
        }
        return d;
    }

    private void sendImage(byte[] data) {
        if (protocol != null) {
            protocol.getBroswerSocket().channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }
    }

    private static class ImageData {
        ImageData(byte[] d) {
            timesp = System.currentTimeMillis();
            data = d;
        }
        long timesp;
        byte[] data;
    }

    private void startCommand(ChannelHandlerContext ctx, Command command) {
        String str = command.getString("type", null);
        if (str != null) {
            if (str.equals("minicap")) {
                startMinicap(command);
            } else if (str.equals("minitouch")) {
                startMinitouch(command);
            }
        }
    }

    private void waittingCommand(ChannelHandlerContext ctx, Command command) {
        setWaitting(true);
    }

    private void keyeventCommand(Command command) {
        System.out.println(command.getContent());
        int k = Integer.parseInt(command.getContent());
        protocol.getMinitouch().sendKeyEvent(k);
    }


    private void touchCommand(ChannelHandlerContext ctx, Command command) {
        String str = (String) command.getContent();
        protocol.getMinitouch().sendEvent(str);
    }

    private void inputCommand(Command command) {
        String str = (String) command.getContent();
        protocol.getMinitouch().inputText(str);
    }

    private void startMinicap(Command command) {
        if (protocol.getMinicap() != null) {
            protocol.getMinicap().kill();
        }

        // 获取请求的配置
        JSONObject obj = (JSONObject) command.get("config");
        Float scale = obj.getFloat("scale");
        Float rotate = obj.getFloat("rotate");
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        Minicap minicap = new Minicap(protocol.getKey());
        minicap.addEventListener(this);
        minicap.start(scale, rotate.intValue());
        protocol.setMinicap(minicap);
    }

    private void startMinitouch(Command command) {
        if (protocol.getMinitouch() != null) {
            protocol.getMinitouch().kill();
        }

        Minitouch minitouch = new Minitouch(protocol.getKey());
        minitouch.addEventListener(this);
        minitouch.start();
        protocol.setMinitouch(minitouch);
    }



}
