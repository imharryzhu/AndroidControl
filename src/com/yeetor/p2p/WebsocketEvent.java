package com.yeetor.p2p;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

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
