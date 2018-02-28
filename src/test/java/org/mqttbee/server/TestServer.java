package org.mqttbee.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.mqttbee.server.packet.handlers.ConnectHandler;
import org.mqttbee.server.packet.handlers.PingReqHandler;

import java.util.logging.Logger;

/**
 * @author Christian Hoff
 */
public class TestServer {
    private final static Logger logger = Logger.getLogger(TestServer.class.getName());

    private final int port;

    private TestServer(final int port) {
        this.port = port;
    }

    private void run() throws InterruptedException {
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
                            ch.pipeline().addLast(new ConnectHandler());
                            ch.pipeline().addLast(new PingReqHandler());
                        }
                    });

            final ChannelFuture future = bootstrap.bind(port);

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(final String[] args) throws InterruptedException {
        final int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 1883;
        }
        logger.info(String.format("running MQTT server on port %d", port));
        new TestServer(port).run();
    }

}