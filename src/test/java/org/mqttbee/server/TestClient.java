package org.mqttbee.server;

import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;

import java.util.logging.Logger;

public class TestClient {
    private final static Logger logger = Logger.getLogger(TestClient.class.getName());

    private final String host;
    private final int port;

    private TestClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    private void run() {
        final Mqtt5Client client = MqttClient.builder()
                .forServerHost(host)
                .forServerPort(port)
                .withIdentifier("TestClient")
                .usingMqtt5()
                .reactive();
        final Mqtt5Connect connect = Mqtt5Connect.builder().withKeepAlive(10).build();
        logger.info(String.format("connect %s:%d", host, port));
        client.connect(connect).subscribe(mqtt5ConnAck -> logger.info("receive ConnAck"), Throwable::printStackTrace);

    }

    public static void main(final String[] args) {
        final String host;
        final int port;
        if (args.length > 0) {
            host = args[0];
        } else {
            host = "localhost";
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        } else {
            port = 1883;
        }
        new TestClient(host, port).run();
    }
}
