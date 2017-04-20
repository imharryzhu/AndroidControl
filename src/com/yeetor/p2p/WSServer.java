package com.yeetor.p2p;

import com.yeetor.minicap.*;
import com.yeetor.minitouch.Minitouch;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

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
                childHandler(new ChildChannel(new WebsocketEvent()));
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }

    private class WebsocketEvent implements IWebsocketEvent {

        @Override
        public void onConnect(ChannelHandlerContext ctx) {
        }

        @Override
        public void onDisconnect(ChannelHandlerContext ctx) {
            for (Protocol protocol : protocolList) {
                if (protocol.getBroswerSocket() != null && protocol.getBroswerSocket() == ctx) {
                    protocol.broswerDisconnect();
                    protocolList.remove(protocol);
                    break;
                }

                if (protocol.getClientSocket() != null && protocol.getClientSocket() == ctx) {
                    protocol.clientDisconnect();
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
            } else if (text.startsWith("config://")) {
                Protocol protocol = findProtocolByBrowser(ctx);
                if (protocol != null) {
                    String s = text.split("://")[1];
                    float scale = Float.parseFloat(s.split(":")[0]);
                    int ro = Integer.parseInt(s.split(":")[1]);
                    protocol.getMinicap().reStart(scale, ro);
                }
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
            Minicap cap = new Minicap();
            cap.addEventListener(localClient);
            cap.start(0.3f, 0);

            // 启动touch
            Minitouch touch = new Minitouch();
            touch.addEventListener(localClient);
            touch.start();


            protocol.setMinicap(cap);
            protocol.setMinitouch(touch);
            protocol.setLocalClient(localClient);
        }

        void sendImage(ChannelHandlerContext ctx, byte[] data) {
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
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
