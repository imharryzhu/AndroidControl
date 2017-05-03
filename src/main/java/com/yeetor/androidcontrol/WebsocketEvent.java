package com.yeetor.androidcontrol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * Created by harry on 2017/4/18.
 */
public abstract class WebsocketEvent {
    abstract void onConnect(ChannelHandlerContext ctx);
    abstract void onDisconnect(ChannelHandlerContext ctx);
    abstract void onTextMessage(ChannelHandlerContext ctx, String text);
    abstract void onBinaryMessage(ChannelHandlerContext ctx, byte[] data);
    DefaultFullHttpResponse onHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        return null;
    }
}
