package com.yeetor.p2p;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by harry on 2017/4/18.
 */
public interface IWebsocketEvent {
    void onConnect(ChannelHandlerContext ctx);
    void onDisconnect(ChannelHandlerContext ctx);
    void onTextMessage(ChannelHandlerContext ctx, String text);
    void onBinaryMessage(ChannelHandlerContext ctx, byte[] data);
}
