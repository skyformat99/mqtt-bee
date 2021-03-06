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

package org.mqttbee.api.mqtt.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5AuthBuilder {

    @NotNull
    Mqtt5AuthBuilder withData(@Nullable byte[] data);

    @NotNull
    Mqtt5AuthBuilder withData(@Nullable ByteBuffer data);

    @NotNull
    Mqtt5AuthBuilder withReasonString(@Nullable String reasonString);

    @NotNull
    Mqtt5AuthBuilder withReasonString(@Nullable MqttUTF8String reasonString);

    @NotNull
    Mqtt5AuthBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
