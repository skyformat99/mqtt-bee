package org.mqttbee.server.packet.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Christian Hoff
 */
public class ConnectHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        boolean release = true;
        try {
            final ByteBuf byteBuf = (ByteBuf) msg;
            if (byteBuf.readByte() == 0b0001_0000) {
                final ByteBuf connAck = ctx.alloc().ioBuffer();
                final byte[] encodedConnAck = {
                        // fixed header
                        //   type, flags
                        0b0010_0000,
                        //   remaining length
                        3,
                        // variable header
                        //   connack flags
                        0b0000_0000,
                        //   reason code (success)
                        0x00,
                        //   property length
                        0
                };
                connAck.writeBytes(encodedConnAck);
                ctx.writeAndFlush(connAck);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
