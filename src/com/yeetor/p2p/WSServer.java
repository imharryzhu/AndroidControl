package com.yeetor.p2p;

import com.alibaba.fastjson.JSON;
import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.*;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.util.Util;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by harry on 2017/4/18.
 */
public class WSServer {

    private int port = -1;
    List<Protocol> protocolList;

    public WSServer(int port) {
        listen(port);
        protocolList = new LinkedList<Protocol>();
    }

    public void listen(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).
                channel(NioServerSocketChannel.class).
                option(ChannelOption.SO_BACKLOG, 128).
                childOption(ChannelOption.SO_KEEPALIVE, true).
                childHandler(new ChildChannel(new WebsocketEventImp()));
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }

    private class WebsocketEventImp extends WebsocketEvent {

        @Override
        public void onConnect(ChannelHandlerContext ctx) {
        }

        @Override
        public void onDisconnect(ChannelHandlerContext ctx) {
            for (Protocol protocol : protocolList) {
                if (protocol.getBroswerSocket() != null && protocol.getBroswerSocket() == ctx) {
                    protocol.broswerDisconnect();
                    protocol.close();
                    protocolList.remove(protocol);
                    break;
                }

                if (protocol.getClientSocket() != null && protocol.getClientSocket() == ctx) {
                    protocol.clientDisconnect();
                    protocol.close();
                    protocolList.remove(protocol);
                    break;
                }
            }
        }

        @Override
        public void onTextMessage(ChannelHandlerContext ctx, String text) {
            if (text.startsWith("wait://")) {
                wait(ctx, text);
            } else if ("waiting".equals(text)) {
                Protocol protocol = findProtocolByBrowser(ctx);
                if (protocol != null) {
                    protocol.getLocalClient().setWaitting(true);
                }
            } else if (text.startsWith("keyevent://")) {


            } else if (text.startsWith("config://")) {
                Protocol protocol = findProtocolByBrowser(ctx);
                if (protocol != null) {
                    String s = text.split("://")[1];
                    float scale = Float.parseFloat(s.split(":")[0]);
                    int ro = Integer.parseInt(s.split(":")[1]);
                    protocol.getMinicap().reStart(scale, ro);
                }
            } else if (text.equals("devices")){
                sendDevicesJson(ctx);
            } else if (text.startsWith("shot://")){
                String sn = text.split("://")[1];
                sendShot(ctx, sn);
            } else { // touch message
                Protocol protocol = findProtocolByBrowser(ctx);
                if (protocol != null) {
                    protocol.getMinitouch().sendEvent(text);
                }
            }
        }

        @Override
        public void onBinaryMessage(ChannelHandlerContext ctx, byte[] data) {
        }

        @Override
        DefaultFullHttpResponse onHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            // TODO 目前只支持GET请求
            if (req.method().toString().toUpperCase().equals("GET")) {
                DefaultFullHttpResponse response = onHttpGet(req.uri());
                return response;
            } else {
                return null;
            }
        }

        void wait(final ChannelHandlerContext ctx, String text) {
            String key = text.substring("wait://".length());
            // TODO 目前在本地跑，所以不需要key来验证
//            if (key.isEmpty()) {
//                ctx.channel().close();
//                return;
//            }

            // TODO 目前直接通知客户端准备完毕

            ctx.channel().writeAndFlush(new TextWebSocketFrame("open://" + key));

            Protocol protocol = new Protocol();
            protocol.setKey(key);
            protocol.setBroswerSocket(ctx);
            protocolList.add(protocol);

            // 启动minicap
            LocalClient localClient = new LocalClient(protocol);
            Minicap cap = new Minicap(key);
            cap.addEventListener(localClient);
            cap.start(0.3f, 0);

            // 启动touch
            Minitouch touch = new Minitouch(key);
            touch.addEventListener(localClient);
            touch.start();


            protocol.setMinicap(cap);
            protocol.setMinitouch(touch);
            protocol.setLocalClient(localClient);
        }

        void sendDevicesJson(ChannelHandlerContext ctx) {
            IDevice[] devices = AdbServer.server().getDevices();
            ArrayList<DeviceInfo> list = new ArrayList<DeviceInfo>();
            for (IDevice device : devices) {
                list.add(new DeviceInfo(device)); // TODO 耗时长，需优化
            }
            String json = JSON.toJSONString(list);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
        }

        void sendShot(ChannelHandlerContext ctx, String sn) {
            Minicap cap = new Minicap(sn);
            ctx.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(cap.takeScreenShot())));
        }

        void sendImage(ChannelHandlerContext ctx, byte[] data) {
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }

        DefaultFullHttpResponse onHttpGet(String uri) {

            if (uri.startsWith("/shot")) {
                // 获取serialNumber
                String[] s = uri.split("/");
                if (s.length == 3) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(new Minicap(s[2]).takeScreenShot()));
                } else {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                }
            }
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        }
    }

    private Protocol findProtocolByBrowser(ChannelHandlerContext ctx) {
        for (Protocol protocol : protocolList) {
            if (protocol.getBroswerSocket() != null && protocol.getBroswerSocket() == ctx) {
                return protocol;
            }
        }
        return null;
    }

    private Protocol findProtocolByClient(ChannelHandlerContext ctx) {
        for (Protocol protocol : protocolList) {
            if (protocol.getClientSocket() != null && protocol.getClientSocket() == ctx) {
                return protocol;
            }
        }
        return null;
    }

}
