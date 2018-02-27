package org.mqttbee.server.packet.handlers;

import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

public class PacketHandlerUtil {
    public static boolean isMessageType(final short fixedHeaderByte1, final Mqtt5MessageType packetType) {
        final int packetTypeCode = fixedHeaderByte1 >> 4;
        return Mqtt5MessageType.fromCode(packetTypeCode) == packetType;
    }
}
