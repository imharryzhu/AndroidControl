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

import com.yeetor.adb.AdbServer;
import com.yeetor.adb.AdbUtils;
import com.yeetor.minicap.Minicap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;

public class HttpServer {
    private static Logger logger = Logger.getLogger(HttpServer.class);
    public void onRequest(ChannelHandlerContext ctx, HttpRequest request) {
        String uri = request.uri();
        
        if (uri.startsWith("/devices")) {
            devices(ctx, request);
        } else if(uri.startsWith("/shot")) {
            shot(ctx, request);
        } else {
            doCommonResponse(ctx, request, HttpResponseStatus.NOT_FOUND);
        }
    }
    
    public void shot(ChannelHandlerContext ctx, HttpRequest request) {
        
        String uri = request.uri();
        String[] args = uri.split("/");
        
        if (args.length < 1 || args[1].length() == 0) {
            doCommonResponse(ctx, request, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        
        String serialNumber = args[2];

        long startTime=System.currentTimeMillis();
        Minicap cap = new Minicap(serialNumber);
        byte[] data = cap.takeScreenShot(); // TODO 性能优化，现在速度慢
        long endTime=System.currentTimeMillis();
        logger.info("ScreenShot used：" + (endTime - startTime) + "ms");


        doDefaultResponse(ctx, request, data, "image/jpeg");
    }

    public void devices(ChannelHandlerContext ctx, HttpRequest request) {

        String uri = request.uri();

        String json = AdbUtils.devices2JSON();
        byte[] data = json.getBytes();

        doDefaultResponse(ctx, request, data, "text/json");
    }
    
    public void doCommonResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().add("Content-Type", "text/json");
        response.headers().add("Server", "Yeetor");

        String json = "";
        byte[] data = json.getBytes();

        response.content().writeBytes(data);
        response.headers().add("Content-Length", data.length);
        boolean isKeepAlive = HttpUtil.isKeepAlive(request);

        if (!isKeepAlive || response.status() != HttpResponseStatus.OK) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }
    
    public void doDefaultResponse(ChannelHandlerContext ctx,  HttpRequest request, byte[] data, String contentType) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Content-Type", contentType);
        response.headers().add("Server", "Yeetor");

        response.content().writeBytes(data);
        response.headers().add("Content-Length", data.length);
        boolean isKeepAlive = HttpUtil.isKeepAlive(request);

        if (!isKeepAlive || response.status() != HttpResponseStatus.OK) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }
    
}
