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

package org.mqttbee.mqtt.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscribe extends
        MqttWrappedMessage<MqttSubscribe, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
        implements Mqtt5Subscribe {

    private final ImmutableList<MqttSubscription> subscriptions;

    public MqttSubscribe(
            @NotNull final ImmutableList<MqttSubscription> subscriptions,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttSubscribe, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.subscriptions = subscriptions;
    }

    @NotNull
    @Override
    public ImmutableList<MqttSubscription> getSubscriptions() {
        return subscriptions;
    }

    @NotNull
    @Override
    protected MqttSubscribe getCodable() {
        return this;
    }

    public MqttSubscribeWrapper wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttSubscribeWrapper(this, packetIdentifier, subscriptionIdentifier);
    }

}
