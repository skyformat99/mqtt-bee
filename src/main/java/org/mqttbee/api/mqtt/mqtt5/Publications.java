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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.rx.FlowableWithSingle;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class Publications {

    private FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe;
    private CompletableFuture<Mqtt5SubAck> subAckFuture;
    private Consumer<Mqtt5Publish> publishConsumer;
    private BlockingQueue<Mqtt5Publish> publishQueue;

    public Publications(
            @NotNull final FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe) {
        this.subscribe = subscribe;
        subAckFuture = new CompletableFuture<>();
        publishQueue = new LinkedBlockingDeque<>(1);
    }

    public Publications forEachPublication(@NotNull final Consumer<Mqtt5Publish> publishConsumer) {
        this.publishConsumer = publishConsumer;
        return this;
    }

    public Publications onSubscriptionSuccess(@NotNull final Consumer<Mqtt5SubAck> subAckConsumer) {
        subAckFuture.thenAccept(subAckConsumer);
        return this;
    }

    public Publications onError(@NotNull final Consumer<Throwable> throwableConsumer) {
        subAckFuture.handle((suback, exception) -> {
            throwableConsumer.accept(exception);
            return null;
        });
        return this;
    }

    void publicationArrived(@NotNull final Mqtt5Publish publish) throws InterruptedException {
        publishConsumer.accept(publish);
        publishQueue.put(publish);
    }

    void subAckArrived(@NotNull final Mqtt5SubAck subAck) {
        subAckFuture.complete(subAck);
    }

    void errorHappened(@NotNull final Throwable error) {
        subAckFuture.completeExceptionally(error);
    }

    @Nullable
    public Mqtt5SubAck getSubAck() throws InterruptedException {
        try {
            return subAckFuture.get();
        } catch (ExecutionException e) {
            return null;
        }
    }

    public Mqtt5Publish getNextPublication(@NotNull final int timeout, @NotNull final TimeUnit timeUnit)
            throws InterruptedException, TimeoutException {

        final Mqtt5Publish publish = publishQueue.poll(timeout, timeUnit);
        if (publish == null) {
            throw new TimeoutException("no publish arrived in time:" + timeout + " " + timeUnit);
        }
        return publish;
    }
}
