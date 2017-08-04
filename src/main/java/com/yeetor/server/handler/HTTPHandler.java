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

package com.yeetor.server.handler;

import com.yeetor.server.HttpServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;

public class HTTPHandler extends SimpleChannelInboundHandler<Object> {
    
    HttpServer server;
    
    public HTTPHandler(HttpServer server) {
        this.server = server;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            throw new IllegalArgumentException("Not a http request!");
        }
        
        HttpRequest request = (HttpRequest) msg;
        
        server.onRequest(ctx, request);
        
//        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        response.headers().add("Content-Type", "text/html");
//        response.headers().add("Server", "Yeetor");
//
//        byte[] s = "aaaa\0".getBytes();
//        
//        
//        response.content().writeBytes(Unpooled.wrappedBuffer(s));
//        response.headers().add("Content-Length", s.length);
//        boolean isKeepAlive = HttpUtil.isKeepAlive(request);
//        
//        if (!isKeepAlive || response.status() != HttpResponseStatus.OK) {
//            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); 
//        } else {
//            response.headers().set(CONNECTION, KEEP_ALIVE);
//            ctx.writeAndFlush(response);
//        }
    }
}
 