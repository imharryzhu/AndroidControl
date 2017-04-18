package com.yeetor.p2p;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by harry on 2017/4/18.
 */
public class Protocol {

    public ChannelHandlerContext broswerSocket;
    public ChannelHandlerContext clientSocket;
    public String key;

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

    public ChannelHandlerContext getBroswerSocket() {
        return broswerSocket;
    }

    public ChannelHandlerContext getClientSocket() {
        return clientSocket;
    }

    public String getKey() {
        return key;
    }
}
