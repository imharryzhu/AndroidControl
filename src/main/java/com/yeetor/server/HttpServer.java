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

import com.yeetor.adb.AdbUtils;
import com.yeetor.minicap.Minicap;
import com.yeetor.util.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import org.apache.log4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class HttpServer {
    private static Logger logger = Logger.getLogger(HttpServer.class);
    private static String INDEX_FILE = "index.html";
    
    public HttpServer() {
        
    }
    
    public void onRequest(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response) {
        String uri = request.uri();
        logger.info("http:" + uri);
        
        // 找到符合注解的方法
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof HttpRouter) {
                    String routeUri = ((HttpRouter) annotation).uri();
                    if (uri.startsWith(routeUri)) {
                        try {
                            method.invoke(this, ctx, request, response);
                            return;
                        } catch (Exception e) {
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        doFileRequest(ctx, request, response);
    }

    public void doFileRequest(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response) {
        String location = request.uri().substring(1);
        if (location.equals("")) {
            location = INDEX_FILE;
        }

        File localFile = new File(Constant.getResourceDir(), "web" + File.separator + location);

        if (!localFile.exists() || localFile.isHidden()) {
            writeErrorHttpResponse(ctx, request, response, HttpResponseStatus.NOT_FOUND);
            return;
        }
        
        try{
            RandomAccessFile raf = new RandomAccessFile(localFile, "r");
            ChunkedFile chunkedFile = new ChunkedFile(raf, 0, localFile.length(), 8192);
            long fileSize = chunkedFile.length();

            response.headers().add(CONTENT_LENGTH, localFile.length());
            
            MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
            response.headers().add(CONTENT_TYPE, mimetypesFileTypeMap.getContentType(localFile.getPath()));


            writeResponse(ctx, request, response, chunkedFile);
                    
        }catch (IOException e) {
            writeErrorHttpResponse(ctx, request, response, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }
    }
    
    public HttpResponse doDefaultResponse(ChannelHandlerContext ctx,  HttpRequest request, byte[] data, String contentType) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Content-Type", contentType);
        response.content().writeBytes(data);
        response.headers().add("Content-Length", data.length);
        return response;
    }
    
    @HttpRouter(uri="/devices")
    public void devices(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response) {
        String json = AdbUtils.devices2JSON();
        response.headers().set(CONTENT_TYPE, "text/plain");
        writeHttpResponseWithString(ctx, request, response, json);
    }

    @HttpRouter(uri="/shot")
    public void shot(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response) {
        String uri = request.uri();
        String[] args = uri.split("/");
        if (args.length < 1 || args[1].length() == 0) {
            writeErrorHttpResponse(ctx, request, response, HttpResponseStatus.NOT_FOUND);
            return;
        }
        
        String serialNumber = args[2];
        long startTime=System.currentTimeMillis();
        Minicap cap = new Minicap(serialNumber);
        byte[] data = cap.takeScreenShot();
        long endTime=System.currentTimeMillis();
        logger.info("ScreenShot used：" + (endTime - startTime) + "ms");
        response.headers().set(CONTENT_TYPE, "image/jpeg");
        HttpContent content = new DefaultHttpContent(Unpooled.wrappedBuffer(data));
        response.headers().set(CONTENT_LENGTH, content.content().readableBytes());
        writeResponse(ctx, request, response, content);
    }

    /**
     * 发送Response，
     * @param ctx
     * @param response
     */
    public void writeHttpResponseWithString(ChannelHandlerContext ctx,  HttpRequest request, HttpResponse response, String dataString) {
        HttpContent content = new DefaultHttpContent(Unpooled.wrappedBuffer(dataString.getBytes()));
        response.headers().set(CONTENT_LENGTH, content.content().readableBytes());
        ctx.write(response);
        ctx.write(content);
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
    
    public void writeErrorHttpResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response, HttpResponseStatus status) {
        response.setStatus(status);
        response.headers().set(CONTENT_LENGTH, 0);
        writeResponse(ctx, request, response);
    }
    
    public void writeResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response, Object... contents) {

        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);
        for (Object content : contents) {
            ctx.write(content);
        }
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (!HttpUtil.isKeepAlive(request)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
}

