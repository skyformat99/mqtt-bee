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

package org.mqttbee.mqtt5;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.*;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.rx.FlowableWithSingle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Mqtt5ClientImpl implements Mqtt5Client {
    private Mqtt5ClientRx mqttClientRx;

    public Mqtt5ClientImpl(@NotNull final Mqtt5ClientRx mqttClientRx) {
        this.mqttClientRx = mqttClientRx;
    }

    @Override
    public ConnectFuture connect(@NotNull final Mqtt5Connect connect) {
        final ConnectFuture future = new ConnectFuture();
        mqttClientRx.connect(connect).doOnSuccess(connack -> future.complete(connack));
        return future;
    }

    @Override
    public Publications subscribe(@NotNull final Mqtt5Subscribe subscribe) {
        final FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribeFlowable =
                mqttClientRx.subscribe(subscribe);
        return new Publications(subscribeFlowable);
    }

    @Override
    public PublishFuture publish(@NotNull final Mqtt5Publish mqtt5Publish) {
        // how to init the stream without having to create a flowable on each publish?
        final Flowable<Mqtt5Publish> publishFlowable = Flowable.create(emitter -> {
        }, BackpressureStrategy.MISSING);
        mqttClientRx.publish(publishFlowable);
        final PublishFuture publishFuture = new PublishFuture();
        return publishFuture;
    }

    @Override
    public Mqtt5ConnAck connectAndWait(
            @NotNull final Mqtt5Connect connect) throws InterruptedException, ExecutionException {
        return connect(connect).get();
    }

    @Override
    public Mqtt5ConnAck connectAndWait(@NotNull final Mqtt5Connect connect, @NotNull final int timeout, @NotNull final TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return connect(connect).get(timeout, timeUnit);
    }
}
