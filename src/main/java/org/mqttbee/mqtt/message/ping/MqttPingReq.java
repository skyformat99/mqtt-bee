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

package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttPingReqEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPingReq implements MqttMessage, Mqtt5PingReq {

    public static final MqttPingReq INSTANCE = new MqttPingReq();

    private MqttPingReq() {
    }

    @NotNull
    @Override
    public MqttMessageEncoder getEncoder() {
        return MqttPingReqEncoder.INSTANCE;
    }

}
