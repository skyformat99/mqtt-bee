/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;

/**
 * @author Christian Hoff
 * @author David Katz
 */
class Mqtt5PubAckEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    Mqtt5PubAckEncoderTest() {
        super(true);
    }

    @Test
    void encode_simple() {
        // MQTT v5.0 Spec §3.4
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                127, 1,
                //   PUBACK reason code
                0x00,
                //   properties length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final MqttPubAck pubAck =
                new MqttPubAck((127 * 256) + 1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonCodeSuccess() {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubAckReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_doNotOmitNonSuccessReasonCodes(final Mqtt5PubAckReasonCode reasonCode) {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                (byte) reasonCode.getCode()
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck = new MqttPubAck(1, reasonCode, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonStringIfMaxSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.2
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitUserPropertyIfMaxAllowedSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.3
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUserPropertiesImpl userProperties = getUserProperties(1);

        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, null, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonStringAndUserPropertyIfMaxAllowedSizeTooSmall() {
        // MQTT v5.0 Spec §3.4.2.2.3
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x10
        };
        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, reasonString, userProperties,
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSizeExceededOnSuccess_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, maxPacket.getTooManyUserProperties(1),
                        Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_multipleUserProperties() {
        // MQTT v5.0 Spec §3.4.2.2
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x00,
                //   property length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);

        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSize() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        // MQTT v5.0 Spec §3.4.1
        final ByteBuf expected = Unpooled.buffer(MAXIMUM_PACKET_SIZE_LIMIT);
        expected.writeBytes(new byte[]{
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                0x00
        });

        final int maxPropertyLength = getMaxPropertyLength();
        // property length
        MqttVariableByteInteger.encode(maxPropertyLength, expected);

        final int remainingBytes = maxPacket.getRemainingPropertyBytes(); // reason string identifier and length
        // reason string
        final int reasonStringLength = remainingBytes - 3;
        expected.writeByte(0x1F);
        final MqttUTF8StringImpl reasonString = getPaddedUtf8String(reasonStringLength);
        reasonString.to(expected);

        final MqttUserPropertiesImpl maxPossibleUserProperties = maxPacket.getMaxPossibleUserProperties();
        maxPossibleUserProperties.encode(expected);

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, maxPossibleUserProperties,
                        Mqtt5PubAckEncoder.PROVIDER);

        encode(expected.array(), pubAck);
        expected.release();
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, maxPacket.getTooManyUserProperties(1),
                        Mqtt5PubAckEncoder.PROVIDER);

        encode(expected, pubAck);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttUserPropertiesImpl maxUserProperties = maxPacket.getMaxPossibleUserProperties();

        final ByteBuf expected = Unpooled.buffer(MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes(),
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes());

        // fixed header
        // type, reserved
        expected.writeByte(0b0100_0000);
        // remaining length (2 + 1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount) = 268435447
        expected.writeByte(0xf7);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // packet identifier
        expected.writeByte(0);
        expected.writeByte(1);
        // reason code
        expected.writeByte(0x00);
        // properties length = 268435440
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        maxUserProperties.encode(expected);

        final MqttUTF8StringImpl reasonString = getPaddedUtf8String(maxPacket.getRemainingPropertyBytes() + 10);
        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5PubAckEncoder.PROVIDER);

        encode(expected.array(), pubAck);
        expected.release();
    }

    @Test
    void encode_propertyLengthExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttUserPropertiesImpl userProperties =
                getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes));
        final int reasonStringTooLong = (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE % userPropertyBytes) + 1;
        final MqttUTF8StringImpl reasonString = getPaddedUtf8String(reasonStringTooLong);

        final ByteBuf expected = Unpooled.buffer(MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes(),
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes());

        // fixed header
        // type, reserved
        expected.writeByte(0b0100_0000);
        // remaining length (2 + 1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount) = 268435447
        expected.writeByte(0xf7);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // packet identifier
        expected.writeByte(0);
        expected.writeByte(1);
        // reason code
        expected.writeByte(0);
        // properties length
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        userProperties.encode(expected);

        final MqttPubAck pubAck = new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, reasonString, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected.array(), pubAck);
        expected.release();
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                127, 1
        };

        final MqttUserPropertiesImpl userProperties =
                getUserProperties(VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes + 1);
        final MqttPubAck pubAck = new MqttPubAck((127 * 256) + 1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties,
                Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @Test
    void encode_omitReasonCodeAndPropertyLength() {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 1
        };

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

        final MqttPubAck pubAck =
                new MqttPubAck(1, Mqtt5PubAckReasonCode.SUCCESS, null, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5PubAckReasonCode.class)
    void encode_reasonCodes(final Mqtt5PubAckReasonCode reasonCode) {
        // MQTT v5.0 Spec §3.4.2.1
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b0100_0000,
                //   remaining length
                30,
                // variable header
                //   packet identifier
                0, 1,
                //   PUBACK reason code
                (byte) reasonCode.getCode(),
                //   properties length
                26,
                //   properties
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);

        final MqttPubAck pubAck =
                new MqttPubAck(1, reasonCode, reasonString, userProperties, Mqtt5PubAckEncoder.PROVIDER);
        encode(expected, pubAck);
    }

    private void encode(final byte[] expected, final MqttPubAck pubAck) {
        encode(pubAck, expected);
    }

    int getMaxPropertyLength() {
        return MAXIMUM_PACKET_SIZE_LIMIT - 1 // type, reserved
                - 4  // remaining length
                - 2  // packet identifier
                - 1  // reason code
                - 4; // property length
    }
}
