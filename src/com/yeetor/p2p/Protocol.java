package com.yeetor.p2p;

import com.yeetor.minicap.Minicap;
import com.yeetor.minitouch.Minitouch;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by harry on 2017/4/18.
 */
public class Protocol {

    public ChannelHandlerContext broswerSocket;
    public ChannelHandlerContext clientSocket;
    public String key;
    public Minicap minicap;
    public Minitouch minitouch;
    public LocalClient localClient;

    public void broswerDisconnect() {
        if (clientSocket != null) {
            clientSocket.channel().close();
        }
    }

    public void clientDisconnect() {
        if (broswerSocket != null) {
            broswerSocket.channel().close();
        }
    }

    public void setBroswerSocket(ChannelHandlerContext broswerSocket) {
        this.broswerSocket = broswerSocket;
    }

    public void setClientSocket(ChannelHandlerContext clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMinicap(Minicap minicap) {
        this.minicap = minicap;
    }

    public void setMinitouch(Minitouch minitouch) {
        this.minitouch = minitouch;
    }

    public void setLocalClient(LocalClient localClient) {
        this.localClient = localClient;
    }

    public ChannelHandlerContext getBroswerSocket() {
        return broswerSocket;
    }

    public ChannelHandlerContext getClientSocket() {
        return clientSocket;
    }

    public String getKey() {
        return key;
    }

    public Minicap getMinicap() {
        return minicap;
    }

    public Minitouch getMinitouch() {
        return minitouch;
    }

    public LocalClient getLocalClient() {
        return localClient;
    }
}
