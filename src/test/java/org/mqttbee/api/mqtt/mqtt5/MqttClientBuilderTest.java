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

import io.reactivex.Flowable;
import org.junit.Test;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.rx.FlowableWithSingle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MqttClientBuilderTest {

    @Test
    public void clientBuilderCreated_ok() {
        final String host = "host";
        final int port = 1883;
        final Mqtt5ClientRx mqtt5ClientRx =
                MqttClient.builder().forServerHost(host).forServerPort(port).usingMqtt5().reactive();
        final Mqtt5Connect connectionRequest = Mqtt5Connect.builder().withCleanStart(true).withKeepAlive(60).build();
        mqtt5ClientRx.connect(connectionRequest).doOnSuccess((connAckResult) -> {
            System.out.println("connected:" + connAckResult);
        });

        final String topicFilter = "topic";
        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().withQoS(MqttQoS.AT_LEAST_ONCE).withTopicFilter(topicFilter).build();
        Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("user", "property");
        Mqtt5UserProperties userProperties = Mqtt5UserProperties.of(userProperty);
        final Mqtt5Subscribe subscribeRequest = Mqtt5Subscribe.builder()
                .addSubscription(subscription)
                .withUserProperties(userProperties)
                .addSubscription(subscription)
                .build();
        final FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe1 =
                mqtt5ClientRx.subscribe(subscribeRequest);
        mqtt5ClientRx.subscribe(subscribeRequest)
                .doOnSingle(
                        (suback, sub) -> System.out.println("received suback" + suback + " for subscription " + sub))
                .doOnEach(publication -> System.out.println("received publication " + publication));

        mqtt5ClientRx.subscribe(subscription)
                .doOnSingle(
                        (suback, sub) -> System.out.println("received suback" + suback + " for subscription 2 " + sub))
                .doOnEach(publication -> System.out.println("received publication " + publication));

        byte[] payload = {1, 2, 3};
        String topic = "mytopic";
        final Mqtt5Publish publishMessage = Mqtt5Publish.builder().withTopic(topic).withPayload(payload).build();

        // is there a better way to publish a single event?
        // Also: is a publsih even happening here, or is an API-side flowable being connected to the out channel, and publishes
        // will happen once something actually flows here? Maybe name is misleading?
        mqtt5ClientRx.publish(Flowable.just(publishMessage));

        final Mqtt5Client mqtt5Client =
                MqttClient.builder().forServerHost(host).forServerPort(port).usingMqtt5().simple();

        ConnectFuture connectFuture = mqtt5Client.connect(connectionRequest).onConnected((connAck) -> {
            System.out.println("connected:" + connAck);
        });

        Mqtt5ConnAck connAck;
        try {
            // a few ways to connect and wait....
            connAck = mqtt5Client.connect(connectionRequest).get();
            connAck = mqtt5Client.connectAndWait(connectionRequest);
            connAck = mqtt5Client.connectAndWait(connectionRequest, 10, TimeUnit.SECONDS);
            System.out.println(connAck);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            System.err.println(e);
        }

        // discussed whether to call this Publications, PublicationFlow (confusion with Rx API) or PublicationStream (confusion with Java Streams API)
        final Publications publications = mqtt5Client.subscribe(subscribeRequest)
                .onSubscriptionSuccess(suback -> System.out.println("suback reason:" + suback.getReasonCodes()))
                .forEachPublication(publication -> System.out.println("received publication " + publication))
                .onError(System.err::println);

        try {
            final Mqtt5SubAck subAck = publications.getSubAck();
            System.out.println("subscription successful:" + subAck);

            boolean running = true;
            while (running) {
                Mqtt5Publish publish;
                try {
                    publish = publications.getNextPublication(1000, TimeUnit.MILLISECONDS);
                    System.out.println(publish);
                } catch (TimeoutException e) {

                }
            }

            final PublishFuture publishFuture = mqtt5Client.publish(publishMessage)
                    .onPublishSuccess(puback -> System.out.println("puback reason:" + puback.getReasonCode()))
                    .onError(System.err::println);

            try {
                publishFuture.get();
            } catch (ExecutionException e) {
                e.getCause();
            }

        } catch (InterruptedException e) {
            System.err.println("interrupted:" + e.getMessage());
        }
    }
}
