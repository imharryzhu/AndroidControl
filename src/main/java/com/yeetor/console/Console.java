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

package com.yeetor.console;

import com.yeetor.adb.AdbServer;
import com.yeetor.androidcontrol.server.BaseServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.RandomUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Console {
    
    private static final String PROMPT = "> ";
    private static final String BANNER = "help 帮助提示\r\nhello 人生格言\r\n";
    
    private static Console instance;
    
    private Map<String, Class> commands;
    
    public static Console getInstance() {
        if (instance == null) {
            instance = new Console();
            instance.commands = new HashMap<>();
            instance.commands.put("help", HelpCommand.class);
            instance.commands.put("hello", HelloCommand.class);
            instance.commands.put("device", DeviceCommand.class);
        }
        return instance;
    }

    /**
     * Console监听端口
     * @param port
     */
    public void listenOnTCP(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new Adapter());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port);

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    class Adapter extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] res = new byte[buf.readableBytes()];
            buf.readBytes(res);
            buf.release();
            
            String str = new String(res).trim();
            
            Class cls = commands.get(str);
            if (cls != null) {
                Constructor constructor = cls.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                Command command = (Command) constructor.newInstance(str);
                sendStringL(ctx, command.execute());
            } else {
                sendString(ctx, "command not found\n");
            }
            
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            sendString(ctx, BANNER);
            sendPrompt(ctx);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            sendPrompt(ctx);
        }
        
        private void sendStringL(ChannelHandlerContext ctx, String text) {
            if (!text.endsWith("\n")) {
                text += "\n";
            }
            sendString(ctx, text);
        }
        
        private void sendString(ChannelHandlerContext ctx, String text) {
            ByteBuf encoded = ctx.alloc().buffer(4 * text.length());
            encoded.writeBytes(text.getBytes());
            ctx.write(encoded);
            ctx.flush();
        }
        
        private void sendPrompt(ChannelHandlerContext ctx) {
            sendString(ctx, PROMPT);
        }
    }
    
    public abstract static class Command {
        private String command;
        public Command(String command) {
            this.command = command;
        }
        
        public abstract String execute();
    }
    
    public static class HelpCommand extends Command {
        
        HelpCommand(String command) {
            super(command);
        }
        
        @Override
        public String execute() {
            return "暂时没有帮助！";
        }
    }
    
    public static class HelloCommand extends Command {
        String[] strings = new String[] {
                "Talking is cheap, show me the code!",
                "支持作者<http://yeetor.com>"
        };

        HelloCommand(String command) {
            super(command);
        }
        
        @Override
        public String execute() {
            return strings[RandomUtils.nextInt(0, strings.length)];
        }
    }
    
    public static class DeviceCommand extends Command {

        public DeviceCommand(String command) {
            super(command);
        }

        @Override
        public String execute() {
            return BaseServer.getDevicesJSON();
        }
    }
    
}
