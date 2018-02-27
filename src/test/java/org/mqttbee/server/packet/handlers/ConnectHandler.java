package org.mqttbee.server.packet.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

import java.util.logging.Logger;

/**
 * @author Christian Hoff
 */
public class ConnectHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        boolean release = true;
        try {
            final ByteBuf byteBuf = (ByteBuf) msg;
            final short fixedHeaderByte1 = byteBuf.readUnsignedByte();
            if (PacketHandlerUtil.isMessageType(fixedHeaderByte1, Mqtt5MessageType.CONNECT)) {
                logger.info("recv Connect");
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
                logger.info("send ConnAck");
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
