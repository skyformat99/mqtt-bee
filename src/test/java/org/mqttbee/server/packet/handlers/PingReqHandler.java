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
public class PingReqHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = Logger.getLogger(PingReqHandler.class.getName());

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        boolean release = true;
        try {
            final ByteBuf byteBuf = (ByteBuf) msg;
//            final short fixedHeaderByte1 = byteBuf.readUnsignedByte();
            final short fixedHeaderByte1 = byteBuf.getUnsignedByte(byteBuf.readerIndex());
            if (PacketHandlerUtil.isMessageType(fixedHeaderByte1, Mqtt5MessageType.PINGREQ)) {
                logger.info("recv PingReq");
                final ByteBuf pingResp = ctx.alloc().ioBuffer();
                final byte[] encodedPingResp = {
                        // fixed header
                        //   type, flags
                        (byte) 0b1101_0000,
                        //   remaining length
                        0
                };
                logger.info("send PingResp");
                pingResp.writeBytes(encodedPingResp);
                ctx.writeAndFlush(pingResp);
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
