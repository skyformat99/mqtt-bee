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

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPublishEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.NewMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt.message.publish.MqttPublishProperty.*;
import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishEncoder extends Mqtt5WrappedMessageEncoder<MqttPublish, MqttPublishWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttPublish, MqttPublishWrapper, MqttPublishEncoderProvider>
            PROVIDER =
            new NewMqttWrappedMessageEncoderProvider<>(Mqtt5PublishEncoder::new, Mqtt5PublishWrapperEncoder.PROVIDER);

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = 0;

        remainingLength += message.getTopic().encodedLength();

        if (message.getQos() != MqttQoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = message.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength +=
                intPropertyEncodedLength(message.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY);
        propertyLength += nullablePropertyEncodedLength(message.getRawPayloadFormatIndicator());
        propertyLength += nullablePropertyEncodedLength(message.getRawContentType());
        propertyLength += nullablePropertyEncodedLength(message.getRawResponseTopic());
        propertyLength += nullablePropertyEncodedLength(message.getRawCorrelationData());
        propertyLength += message.getUserProperties().encodedLength();

        return propertyLength;
    }

    @Override
    void encodeFixedProperties(@NotNull final ByteBuf out) {
        encodeIntProperty(MESSAGE_EXPIRY_INTERVAL, message.getRawMessageExpiryInterval(),
                MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, message.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, message.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, message.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, message.getRawCorrelationData(), out);
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttPublishWrapper wrapper) {
        return Mqtt5PublishWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5PublishWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttPublishWrapper, MqttPublish, MqttPublishEncoderProvider, Mqtt5PublishEncoder> {

        private static final MqttPublishEncoderProvider PROVIDER =
                new MqttPublishEncoderProvider(Mqtt5PubAckEncoder.PROVIDER, Mqtt5PubRecEncoder.PROVIDER);
        private static final MqttMessageWrapperEncoderApplier<MqttPublishWrapper, MqttPublish, Mqtt5PublishEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5PublishWrapperEncoder::new);

        private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

        @Override
        int additionalRemainingLength() {
            int additionalRemainingLength = 0;

            if ((message.getTopicAlias() != DEFAULT_NO_TOPIC_ALIAS) && !message.isNewTopicAlias()) {
                additionalRemainingLength =
                        MqttBinaryData.EMPTY_LENGTH - message.getWrapped().getTopic().encodedLength();
            }

            return additionalRemainingLength;
        }

        @Override
        int additionalPropertyLength() {
            int additionalPropertyLength = 0;

            additionalPropertyLength += shortPropertyEncodedLength(message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS);

            final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
            for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
                additionalPropertyLength += variableByteIntegerPropertyEncodedLength(subscriptionIdentifiers.get(i));
            }

            return additionalPropertyLength;
        }

        @Override
        public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
            final int maximumPacketSize = MqttServerConnectionData.getMaximumPacketSize(channel);

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            final MqttPublish publish = message.getWrapped();

            int flags = 0;
            if (message.isDup()) {
                flags |= 0b1000;
            }
            flags |= publish.getQos().getCode() << 1;
            if (publish.isRetain()) {
                flags |= 0b0001;
            }

            out.writeByte(FIXED_HEADER | flags);

            MqttVariableByteInteger.encode(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            final MqttPublish publish = message.getWrapped();

            if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (message.isNewTopicAlias())) {
                publish.getTopic().to(out);
            } else {
                MqttBinaryData.encodeEmpty(out);
            }

            if (publish.getQos() != MqttQoS.AT_MOST_ONCE) {
                out.writeShort(message.getPacketIdentifier());
            }

            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            final int propertyLength = propertyLength(maximumPacketSize);
            MqttVariableByteInteger.encode(propertyLength, out);

            wrappedEncoder.encodeFixedProperties(out);
            encodeOmissibleProperties(maximumPacketSize, out);

            encodeShortProperty(TOPIC_ALIAS, message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

            final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
            for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
                encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
            }
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ByteBuffer payload = message.getWrapped().getRawPayload();
            if (payload != null) {
                out.writeBytes(payload.duplicate());
            }
        }

    }

}
