package org.mqttbee.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Christian Hoff
 */
public class TestServer {

    public static void main(final String[] args) throws InterruptedException {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
                                    final ByteBuf byteBuf = (ByteBuf) msg;
                                    if (byteBuf.readByte() == 0b0001_0000) {
                                        final ByteBuf connAck = ctx.alloc().ioBuffer();
                                        connAck.writeByte(0b0010_0000);
                                        connAck.writeByte(3);
                                        connAck.writeByte(0);
                                        connAck.writeByte(0);
                                        connAck.writeByte(0);
                                        ctx.writeAndFlush(connAck);
                                    }
                                }
                            });
                        }
                    });

            final ChannelFuture future = bootstrap.bind(1883);

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}