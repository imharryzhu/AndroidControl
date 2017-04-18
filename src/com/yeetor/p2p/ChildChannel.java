package com.yeetor.p2p;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created by harry on 2017/4/18.
 */
public class ChildChannel extends ChannelInitializer<SocketChannel> {

    IWebsocketEvent event;

    public ChildChannel(IWebsocketEvent event) {
        this.event = event;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("http-codec",new HttpServerCodec());
        socketChannel.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
        socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
        socketChannel.pipeline().addLast("handler",new WSSocketHandler(event));
    }
}
