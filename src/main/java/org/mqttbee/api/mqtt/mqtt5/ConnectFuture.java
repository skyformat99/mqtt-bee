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

import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConnectFuture extends CompletableFuture<Mqtt5ConnAck> {

    public ConnectFuture onConnected(Consumer<Mqtt5ConnAck> consumer) {
        thenAccept((connAck) -> consumer.accept(connAck));
        return this;
    }

    public ConnectFuture onError(Consumer<Throwable> consumer) {
        handle((connack, exception) -> {
            consumer.accept(exception);
            return connack;
        });
        return this;
    }
}
