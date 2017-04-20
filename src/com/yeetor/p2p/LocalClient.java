package com.yeetor.p2p;

import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;
import com.yeetor.p2p.Protocol;
import com.yeetor.p2p.WSServer;
import io.netty.buffer.Unpooled;
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

    // minicap启动完毕后
    @Override
    public void onStartup(Minicap minicap, boolean success) {
        if (protocol != null && protocol.getBroswerSocket() != null) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minicap"));
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
            dataQueue.add(new ImageData(data));
            clearObsoleteImage();
        }
    }

    @Override
    public void onStartup(Minitouch minitouch, boolean success) {
        if (protocol != null && protocol.getBroswerSocket() != null && success) {
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("minitouch"));
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

}
