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

package org.mqttbee.mqtt.handler.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlow;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeWithFlow {

    private final MqttSubscribe subscribe;
    private final MqttSubscriptionFlow flow;

    public MqttSubscribeWithFlow(@NotNull final MqttSubscribe subscribe, @NotNull final MqttSubscriptionFlow flow) {
        this.subscribe = subscribe;
        this.flow = flow;
    }

    @NotNull
    public MqttSubscribe getSubscribe() {
        return subscribe;
    }

    @NotNull
    public MqttSubscriptionFlow getFlow() {
        return flow;
    }

    @NotNull
    public MqttStatefulSubscribeWithFlow createStateful(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttStatefulSubscribeWithFlow(
                subscribe.createStateful(packetIdentifier, subscriptionIdentifier), flow);
    }


    public static class MqttStatefulSubscribeWithFlow {

        private final MqttStatefulSubscribe subscribe;
        private final MqttSubscriptionFlow flow;

        private MqttStatefulSubscribeWithFlow(
                @NotNull final MqttStatefulSubscribe subscribe, @NotNull final MqttSubscriptionFlow flow) {

            this.subscribe = subscribe;
            this.flow = flow;
        }

        @NotNull
        public MqttStatefulSubscribe getSubscribe() {
            return subscribe;
        }

        @NotNull
        public MqttSubscriptionFlow getFlow() {
            return flow;
        }

    }

}
