/**
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
 */
package org.mqttbee.api.mqtt.mqtt5;

import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.rx.FlowableWithSingle;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientRxMock implements Mqtt5ClientRx {

    private List<Mqtt5Publish> publishes;

    public Mqtt5ClientRxMock(final List<Mqtt5Publish> publishes) {
        this.publishes = publishes;
    }

    @NotNull
    @Override
    public Single<Mqtt5ConnAck> connect(@NotNull final Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnect.class);

        return Single.<Mqtt5ConnAck>create(connAckEmitter -> {
            connAckEmitter.onSuccess(new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS, true,
                    MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, MqttConnAck.KEEP_ALIVE_FROM_CONNECT,
                    MqttConnAck.CLIENT_IDENTIFIER_FROM_CONNECT, null, MqttConnAckRestrictions.DEFAULT, null, null, null,
                    MqttUserPropertiesImpl.NO_USER_PROPERTIES));
        }).doOnSuccess(connAck -> {
        }).doOnError(throwable -> {
        });
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe(
            @NotNull final Mqtt5Subscribe subscribe) {

        return new FlowableWithSingle(Flowable.fromIterable(publishes), Mqtt5ConnAck.class, Mqtt5Publish.class);
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe(
            Mqtt5Subscription subscription, Mqtt5UserProperty... userProperties) {
        Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder()
                .withUserProperties(Mqtt5UserProperties.of(userProperties))
                .addSubscription(subscription)
                .build();
        return subscribe(subscribe);
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> remainingPublishes() {
        return Flowable.fromIterable(publishes);
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> allPublishes() {
        return Flowable.fromIterable(publishes);
    }

    @NotNull
    @Override
    public Single<Mqtt5UnsubAck> unsubscribe(@NotNull final Mqtt5Unsubscribe unsubscribe) {
        return Single.just(new MqttUnsubAck(10, ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS), null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES));
    }

    @Override
    public Flowable<Mqtt5PublishResult> publish(@NotNull final Flowable<Mqtt5Publish> publishFlowable) {
        return Flowable.empty();
    }

    @NotNull
    @Override
    public Completable reauth() {
        return Completable.complete();
    }

    @NotNull
    @Override
    public Completable disconnect(@NotNull final Mqtt5Disconnect disconnect) {
        return Completable.complete();
    }

    @NotNull
    @Override
    public Mqtt5ClientData getClientData() {
        return new Mqtt5ClientData() {
            @NotNull
            @Override
            public Optional<Mqtt5ClientConnectionData> getClientConnectionData() {
                return Optional.empty();
            }

            @NotNull
            @Override
            public Optional<Mqtt5ServerConnectionData> getServerConnectionData() {
                return Optional.empty();
            }

            @Override
            public boolean followsRedirects() {
                return false;
            }

            @Override
            public boolean allowsServerReAuth() {
                return false;
            }

            @NotNull
            @Override
            public Optional<Mqtt5AdvancedClientData> getAdvancedClientData() {
                return Optional.empty();
            }

            @NotNull
            @Override
            public MqttClientExecutorConfig getExecutorConfig() {
                return null;
            }

            @NotNull
            @Override
            public Optional<MqttClientIdentifier> getClientIdentifier() {
                return Optional.empty();
            }

            @NotNull
            @Override
            public String getServerHost() {
                return null;
            }

            @Override
            public int getServerPort() {
                return 0;
            }

            @Override
            public boolean usesSSL() {
                return false;
            }

            @Override
            public boolean isConnecting() {
                return false;
            }

            @Override
            public boolean isConnected() {
                return false;
            }
        };
    }
}
