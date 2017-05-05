package com.yeetor.androidcontrol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by harry on 2017/4/18.
 */
public class LocalServer {

    private int port = -1;
    List<Protocol> protocolList;

    public LocalServer(int port) {
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
                childOption(ChannelOption.SO_KEEPALIVE, true).
                childHandler(new ChildChannel(new LocalServerWebsocketEventImp()));
        System.out.println("LocalServer will start at port: " + port);
        System.out.println("--------\r\n");
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }

    private class LocalServerWebsocketEventImp extends WebsocketEvent {

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
            Command command = Command.ParseCommand(text);
            if (command != null) {
                switch (command.getSchem()) {
                    case WAIT:
                        initLocalClient(ctx, command);
                        break;
                    case START:
                    case WAITTING:
                    case TOUCH:
                    case KEYEVENT:
                    case INPUT:
                        executeCommand(ctx, command);
                        break;
                    case SHOT:
                        sendShot(ctx, command);
                        break;
                    case DEVICES:
                        sendDevicesJson(ctx);
                        break;
                }
            } else {
                // Invalid Commands
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

        void initLocalClient(final ChannelHandlerContext ctx, Command command) {

            String sn = command.getString("sn", null);
            String key = command.getString("key", null);

            // 没有sn，默认第一个设备
            if (StringUtils.isEmpty(sn)) {
                IDevice iDevice = AdbServer.server().getFirstDevice();
                if (iDevice == null) {
                    ctx.channel().close();
                    return;
                }
                sn = iDevice.getSerialNumber();
            }

            JSONObject obj = new JSONObject();
            obj.put("sn", sn);
            obj.put("key", key);

            ctx.channel().writeAndFlush(new TextWebSocketFrame("open://" + obj.toJSONString()));

            Protocol protocol = new Protocol();
            protocol.setSn(sn);
            protocol.setKey(key);
            protocol.setBroswerSocket(ctx);
            protocolList.add(protocol);

            LocalClient localClient = new LocalClient(protocol);
            protocol.setLocalClient(localClient);
        }

        void executeCommand(ChannelHandlerContext ctx, Command command) {
            Protocol protocol = null;
            // 寻找与之匹配的protocol
            for (Protocol p : protocolList) {
                if (p.getBroswerSocket() != null && p.getBroswerSocket() == ctx) {
                    protocol = p;
                    break;
                }
                if (p.getClientSocket() != null && p.getClientSocket() == ctx) {
                    protocol = p;
                    break;
                }
            }
            if (protocol != null) {
                protocol.getLocalClient().executeCommand(ctx, command);
            }
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

        void sendShot(ChannelHandlerContext ctx, Command command) {
            String sn = command.getString("sn", null);
            Minicap cap = new Minicap(sn);
            ctx.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(cap.takeScreenShot())));
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
