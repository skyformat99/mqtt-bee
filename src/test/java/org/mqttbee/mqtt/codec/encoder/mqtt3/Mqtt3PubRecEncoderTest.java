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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.mqtt3.Mqtt3PubRecView;

class Mqtt3PubRecEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3PubRecEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x50, 0x02, msb, lsb};
        final MqttPubRec pubRec = Mqtt3PubRecView.wrapped(id);
        encode(expected, pubRec);
    }

    @Test
    void encodedRemainingLength() {
    }
}