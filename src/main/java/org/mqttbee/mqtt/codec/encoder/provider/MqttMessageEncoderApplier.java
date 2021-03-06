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

package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Applies a {@link MqttMessage} to a {@link MqttMessageEncoder}.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public interface MqttMessageEncoderApplier<M extends MqttMessage> {

    /**
     * Returns a encoder for a MQTT message that is applied to the given MQTT message.
     *
     * @param message the MQTT message.
     * @return the encoder for the MQTT message encoder.
     */
    @NotNull
    MqttMessageEncoder apply(@NotNull M message);

}
