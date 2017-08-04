/*
 *
 * MIT License
 *
 * Copyright (c) 2017 朱辉 https://blog.yeetor.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.yeetor.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yeetor.adb.AdbDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.adb.AdbUtils;
import com.yeetor.adb.IAdbServerListener;
import com.yeetor.androidcontrol.client.LocalClient;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.minicap.ScreencapBase;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;
import com.yeetor.protocol.BinaryProtocol;
import com.yeetor.protocol.TextProtocol;
import com.yeetor.server.handler.IWebsocketEvent;
import com.yeetor.server.handler.WSHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import javax.xml.soap.Text;
import java.lang.ref.PhantomReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WSServer implements IWebsocketEvent, MinicapListener, MinitouchListener, IAdbServerListener {

    private static Logger logger = Logger.getLogger(WSServer.class);
    
    /**
     * M_WAIT成功绑定的设备
     */
    AdbDevice bindedDevice = null;

    /**
     * Minicap  
     */
    Minicap capService = null;

    /**
     * Minitouch
     */
    Minitouch eventService = null;

    Channel channel = null;

    /**
     * TODO 古老的优化方法
     */
    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<ImageData> dataQueue = new LinkedBlockingQueue<ImageData>();
    
    WSServer() {
        AdbServer.server().addListener(this);
    }
    
    
    @Override
    public void onConnect(ChannelHandlerContext ctx) {
    }

    @Override
    public void onDisconnect(ChannelHandlerContext ctx) {
        if (eventService != null) {
            eventService.kill();
        }
        if (capService != null) {
            capService.kill();
        }
    }

    @Override
    public void onTextMessage(ChannelHandlerContext ctx, String text) {
        TextProtocol protocol = TextProtocol.ParseWithString(text);
        switch (protocol.getProtocolHeader()) {
            case TextProtocol.Header.M_DEVICES:
                onM_DEVICES(ctx, protocol);
                break;
            case TextProtocol.Header.M_WAIT:
                onM_WAIT(ctx, protocol);
                break;
            case TextProtocol.Header.M_START:
                onM_START(ctx, protocol);
                break;
            case TextProtocol.Header.M_WAITTING:
                onM_WAITTING(ctx, protocol);
                break;
            case TextProtocol.Header.M_TOUCH:
                onM_TOUCH(ctx, protocol);
                break;
            case TextProtocol.Header.M_KEYEVENT:
                onM_KEYEVENT(ctx, protocol);
                break;
            default:
                onInvalidProtocl(ctx, protocol);
                break;
        }
    }
    
    @Override
    public void onBinaryMessage(ChannelHandlerContext ctx, byte[] data) {
    }
    
    private void onM_DEVICES(ChannelHandlerContext ctx, TextProtocol protocol) {
        this.channel = ctx.channel();
        
        String devicesJson = AdbUtils.devices2JSON();
        TextProtocol p = TextProtocol.newProtocol(TextProtocol.Header.SM_DEVICES, devicesJson);
        sendProtocolResponse(p);
    }
    
    private void onM_WAIT(ChannelHandlerContext ctx, TextProtocol protocol) {
        this.channel = ctx.channel();
        
        JSONObject obj = (JSONObject) JSON.parse(protocol.getProtocolBody());
        String sn = obj.getString("sn");
        bindedDevice = AdbServer.server().getDevice(sn);
        
        if (bindedDevice != null) {
            TextProtocol resPro = TextProtocol.newProtocol(TextProtocol.Header.SM_OPENED, "");
            sendProtocolResponse(resPro);
        } else {
            // TODO 通知客户端建立连接不成功
            ctx.channel().close();
        }
    }

    private void onM_START(ChannelHandlerContext ctx, TextProtocol protocol) {
        JSONObject obj = (JSONObject) JSON.parse(protocol.getProtocolBody());
        String type = obj.getString("type");
        if ("cap".equals(type)) {
            startCapService(obj);
        } else if ("event".equals(type)) {
            startEventService();
        }
    }

    private void onM_WAITTING(ChannelHandlerContext ctx, TextProtocol protocol) {
        setWaitting(true);
    }

    private void onM_TOUCH(ChannelHandlerContext ctx, TextProtocol protocol) {
        eventService.sendEvent(protocol.getProtocolBody());
    }
    
    private void onM_KEYEVENT(ChannelHandlerContext ctx, TextProtocol protocol) {
        try {
            int key = Integer.parseInt(protocol.getProtocolBody());
            eventService.sendKeyEvent(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void onInvalidProtocl(ChannelHandlerContext ctx, TextProtocol protocol) {
        logger.warn("invalid protocol: " + protocol.getProtocolHeader());
    }
    
    private void startCapService(JSONObject jsonObject) {
        if (capService != null) {
            capService.kill();
        }
        
        Minicap cap = new Minicap(bindedDevice);
        this.capService = cap;
        cap.addEventListener(this);
        
        // 默认配置
        Float scale = 0.3f; 
        Integer rotate = 0;

        JSONObject obj = (JSONObject) jsonObject.get("config");
        if (obj != null) {
            scale = obj.getFloat("scale");
            rotate = obj.getInteger("rotate");
        }
        
        cap.start(scale, rotate);
    }
    
    private void startEventService() {
        if (eventService != null) {
            eventService.kill();
        }
        Minitouch minitouch = new Minitouch(bindedDevice);
        this.eventService = minitouch;
        minitouch.addEventListener(this);
        minitouch.start();
    }
    
    private void sendProtocolResponse(TextProtocol protocol) {
        String text = String.format("%s://%s", protocol.getProtocolHeader(), protocol.getProtocolBody());
        logger.info("ws text to: " + text);
        channel.writeAndFlush(new TextWebSocketFrame(text));
    }
    
    /*****************************************************************************/

    @Override
    public void onStartup(Minicap minicap, boolean success) {
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "cap");
        map.put("stat", "open");
        TextProtocol protocol = TextProtocol.newProtocol(TextProtocol.Header.SM_SERVICE_STATE, JSON.toJSONString(map));
        sendProtocolResponse(protocol);
    }

    @Override
    public void onClose(Minicap minicap) {
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "cap");
        map.put("stat", "close");
        TextProtocol protocol = TextProtocol.newProtocol(TextProtocol.Header.SM_SERVICE_STATE, JSON.toJSONString(map));
        sendProtocolResponse(protocol);
    }

    @Override
    public void onBanner(Minicap minicap, Banner banner) {
    }

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

    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static byte[] toHH(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    private void sendImage(byte[] data) {
        
        
        byte[] head =  new byte[2];
        head[0] = (BinaryProtocol.Header.SM_JPG) & 0xff;
        head[1] = (BinaryProtocol.Header.SM_JPG >> 8) & 0xff;
        
        int len = data.length;
        byte[] lenbuf = new byte[4];
        lenbuf[0] = (byte)((len) & 0xff);
        lenbuf[1] = (byte)((len >> 8) & 0xff);
        lenbuf[2] = (byte)((len >> 16) & 0xff);
        lenbuf[3] = (byte)((len >> 24) & 0xff);
        
        byte[] d = ArrayUtils.addAll(ArrayUtils.addAll(head, lenbuf), data);
        
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(d)));
    }
    
    /*********************************************************************************/
    
    @Override
    public void onStartup(Minitouch minitouch, boolean success) {
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "event");
        map.put("stat", "open");
        TextProtocol protocol = TextProtocol.newProtocol(TextProtocol.Header.SM_SERVICE_STATE, JSON.toJSONString(map));
        sendProtocolResponse(protocol);
    }

    @Override
    public void onClose(Minitouch minitouch) {
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "event");
        map.put("stat", "close");
        TextProtocol protocol = TextProtocol.newProtocol(TextProtocol.Header.SM_SERVICE_STATE, JSON.toJSONString(map));
        sendProtocolResponse(protocol);
    }
    
    /*********************************************************************************/
    
    @Override
    public void onAdbDeviceConnected(AdbDevice device) {
        if (this.channel != null) {
            String devicesJson = AdbUtils.devices2JSON();
            TextProtocol p = TextProtocol.newProtocol(TextProtocol.Header.SM_DEVICES, devicesJson);
            sendProtocolResponse(p);
        }
    }
    
    @Override
    public void onAdbDeviceDisConnected(AdbDevice device) {
        if (this.channel != null) {
            String devicesJson = AdbUtils.devices2JSON();
            TextProtocol p = TextProtocol.newProtocol(TextProtocol.Header.SM_DEVICES, devicesJson);
            sendProtocolResponse(p);
        }
    }
    
    /****************************************************************************/

    public static class ImageData {
        ImageData(byte[] d) {
            timesp = System.currentTimeMillis();
            data = d;
        }
        long timesp;
        byte[] data;
    }
}
