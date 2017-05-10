package com.yeetor.androidcontrol.server;

import com.alibaba.fastjson.JSONObject;
import com.yeetor.androidcontrol.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by harry on 2017/5/4.
 */
public class RemoteServer extends BaseServer {
    private int port = -1;
    List<Protocol> protocolList;

    public RemoteServer(int port) {
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
                childHandler(new ChildChannel(new RemoteServerWebsocketEventImp()));
        System.out.println("RemoteServer will start at port: " + port);
        System.out.println("--------\r\n");
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }

    private class RemoteServerWebsocketEventImp extends WebsocketEvent {
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
            if (command.getSchem() != Command.Schem.WAITTING &&
                    command.getSchem() != Command.Schem.INPUT &&
                    command.getSchem() != Command.Schem.KEYEVENT &&
                    command.getSchem() != Command.Schem.TOUCH) {
                System.out.println(command.getCommandString());
            }

            if (command != null) {
                switch (command.getSchem()) {
                    case WAIT:
                        waitRemoteClient(ctx, command);
                        break;
                    case OPEN:
                        remoteClientOpen(ctx, command);
                        break;
                    case START:
                    case WAITTING:
                    case TOUCH:
                    case KEYEVENT:
                    case INPUT:
                    case SHOT:
                    case DEVICES:
                    case MINICAP:
                    case MINITOUCH:
                    case PUSH:
                    case MESSAGE:
                        forwardCommand(ctx, command);
                        break;
                }
            } else {
                // Invalid Commands
            }
        }

        @Override
        public void onBinaryMessage(ChannelHandlerContext ctx, byte[] data) {
            // 二进制直接转发
            forwardBinary(ctx, data);
        }

        @Override
        public DefaultFullHttpResponse onHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            // TODO 目前只支持GET请求
            if (req.method().toString().toUpperCase().equals("GET")) {
                DefaultFullHttpResponse response = onHttpGet(req.uri());
                return response;
            } else {
                return null;
            }
        }

        void waitRemoteClient(final ChannelHandlerContext ctx, Command command) {
            String sn = command.getString("sn", null);
            if (sn == null) {
                ctx.channel().close();
                return;
            }

            String key = command.getString("key", null);

            Protocol protocol = new Protocol();
            protocol.setSn(sn);
            protocol.setKey(key);
            protocol.setBroswerSocket(ctx);
            protocolList.add(protocol);
        }

        void remoteClientOpen(final ChannelHandlerContext ctx, Command command) {
            String sn = command.getString("sn", null);
            if (sn == null) {
                ctx.channel().close();
                return;
            }
            String key = command.getString("key", null);

            Protocol protocol = findProtocolByKey(key);
            if (protocol == null) {
                System.out.println("can not find key:" + key + " sn:" + sn);
                ctx.channel().close();
                return;
            }

            protocol.setClientSocket(ctx);

            JSONObject obj = new JSONObject();
            obj.put("sn", sn);
            obj.put("key", key);

            // 通知浏览器
            protocol.getBroswerSocket().channel().writeAndFlush(new TextWebSocketFrame("open://" + obj.toJSONString()));
        }

        void forwardCommand(ChannelHandlerContext ctx, Command command) {
            Protocol protocol = findProtocolByBrowser(ctx);
            if (protocol == null) {
                protocol = findProtocolByClient(ctx);
                if (protocol == null) {
                    return;
                }
            }

            ChannelHandlerContext sendTo = null;

            if (ctx == protocol.getBroswerSocket()) {
                sendTo = protocol.getClientSocket();
            } else {
                sendTo = protocol.getBroswerSocket();
            }

            sendTo.channel().writeAndFlush(new TextWebSocketFrame(command.getCommandString()));
        }

        void forwardBinary(ChannelHandlerContext ctx, byte[] data) {
            Protocol protocol = findProtocolByBrowser(ctx);
            if (protocol == null) {
                protocol = findProtocolByClient(ctx);
                if (protocol == null) {
                    return;
                }
            }

            ChannelHandlerContext sendTo = null;

            if (ctx == protocol.getBroswerSocket()) {
                sendTo = protocol.getClientSocket();
            } else {
                sendTo = protocol.getBroswerSocket();
            }

            sendTo.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }

        DefaultFullHttpResponse onHttpGet(String uri) {
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

    private Protocol findProtocolByKey(String key) {
        for (Protocol protocol : protocolList) {
            if (protocol.getBroswerSocket() != null && StringUtils.equals(key, protocol.getKey())) {
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
