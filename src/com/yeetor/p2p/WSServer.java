package com.yeetor.p2p;

import com.android.ddmlib.IDevice;
import com.sun.corba.se.spi.activation.Server;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapInstallException;
import com.yeetor.minicap.MinicapListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by harry on 2017/4/18.
 */
public class WSServer {

    static final int DATA_TIMEOUT = 100; //ms

    private int port = -1;
    List<Protocol> protocolList;

    private boolean isWaitting = false;
    private BlockingQueue<ImageData> dataQueue = new LinkedBlockingQueue<ImageData>();

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
                isWaitting = true;
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

            Protocol protocol = new Protocol();
            protocol.setKey(key);
            protocol.setBroswerSocket(ctx);
            protocolList.add(protocol);

            // 启动minicap
            AdbServer server = new AdbServer();
            IDevice[] devices = server.getDevices();
            IDevice device = null;
            for (IDevice d : devices) {
                if ("-".equals(d.getSerialNumber())) {
                    device = d;
                    break;
                }
            }
            if (device == null && devices.length > 0) {
                device = devices[0];
            }

            try {
                Minicap.installMinicap(device);
            } catch (MinicapInstallException e) {
                e.printStackTrace();
            }

            Minicap cap = new Minicap(device);

            MinicapListener listener = new MinicapListener() {
                public void onStartup(Minicap minicap, boolean success) {
                    System.out.println("start up");
                }
                // banner信息读取完毕
                public void onBanner(Minicap minicap, Banner banner) {
                    System.out.println(banner);
                }
                // 读取到图片信息
                public void onJPG(Minicap minicap, byte[] data) {
                    if (isWaitting) {
                        if (dataQueue.size() > 0) {
                            dataQueue.add(new ImageData(data));
                            long curTS = System.currentTimeMillis();
                            // 挑选没有超时的图片
                            ImageData d = null;
                            while (true) {
                                d = dataQueue.poll();
                                // 如果没有超时，或者超时了但是最后一张图片，也发送给客户端
                                if (curTS - d.timesp < DATA_TIMEOUT || dataQueue.size() == 0) {
                                    break;
                                }
                            }
                            sendImage(ctx, d.data);
                        } else {
                            sendImage(ctx, data);
                        }
                        isWaitting = false;
                    } else {
                        dataQueue.add(new ImageData(data));
                    }
                }
            };

            cap.addEventListener(listener);

            cap.start(0.3f, 0);
            ctx.channel().writeAndFlush(new TextWebSocketFrame("open://" + protocol.key));
            ctx.channel().writeAndFlush(new TextWebSocketFrame("minicap"));
        }

        void sendImage(ChannelHandlerContext ctx, byte[] data) {
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }
    }

    static class ImageData {
        ImageData(byte[] d) {
            timesp = System.currentTimeMillis();
            data = d;
        }
        long timesp;
        byte[] data;
    }
}
