package de.xdevsoftware;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Main
{
    private static final Logger logger = LogManager.getLogger(Main.class);

    private static final String MQTT_BROKER_URL = "broker.emqx.io";
    private static final int TCP_PORT = 1883;
    private static final int TLS_PORT = 8883;
    private static final String MQTT_MESSAGE = "hello devnexus";
    private static final String MQTT_TOPIC = "topic/testdevnexus";

    public static void main(String[] args)
    {
        Mqtt3AsyncClient client = createClientTCP();

        client.connect().whenComplete((connectResult, throwable) ->
        {
            if (throwable != null)
            {
                logger.error("Connection failed", throwable);
            }
            else
            {
                logger.info("Connected to the MQTT broker");
                subscribeToTopic(client);
                publishToTopic(client, MQTT_TOPIC, MQTT_MESSAGE);
            }
        });

    }

    private static Mqtt3AsyncClient createClientTCP()
    {
        return MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost(MQTT_BROKER_URL)
                .serverPort(TCP_PORT)
                .buildAsync();
    }

    private static Mqtt3AsyncClient createClientTLS()
    {
        try
        {
            return MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost(MQTT_BROKER_URL)
                    .serverPort(TLS_PORT)
                    .sslWithDefaultConfig()
                    .sslConfig()
                    .applySslConfig()
                    .buildAsync();
        }
        catch (Exception e)
        {
            logger.error("Failed to initialize SSLContext", e);
            throw new RuntimeException("TLS configuration failed", e);
        }
    }

    // qos AT_MOST_ONCE (0): MQTT Client sending the message only once. It may happen that the message get lost.
    private static void publishToTopic(Mqtt3AsyncClient client, String topic, String message)
    {
        client.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_MOST_ONCE)
                .send()
                .whenComplete((publish, exceptions) ->
                {
                    if (exceptions != null)
                    {
                        logger.error("Failed to publish message to topic {}", topic, exceptions);
                    }
                    else
                    {
                        logger.info("Message successfully published to topic {}", topic);
                    }
                });
    }

    private static void subscribeToTopic(Mqtt3AsyncClient client)
    {
        client.subscribeWith()
                .topicFilter(MQTT_TOPIC)
                .callback(publish ->
                {
                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    logger.info("Received message: {}", payload);

                    client.disconnect().whenComplete(
                            (disconnectResult, throwable) -> logger.info("Disconnected from the MQTT broker"));
                })
                .send()
                .whenComplete((subAck, throwable) ->
                {
                    if (throwable != null)
                    {
                        logger.error("Failed to subscribe to topic", throwable);
                    }
                    else
                    {
                        logger.info("Successfully subscribed to topic");
                    }
                });
    }

    // This method will create an MQTT Client with username and password.
    // We are using a public broker for testing, we can't configure username and password in the broker.
    // So this won't work in our case, but it is an example how it could work.
    private static Mqtt3AsyncClient createClientUsernamePassword()
    {
        return MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost(MQTT_BROKER_URL)
                .serverPort(TCP_PORT)
                .simpleAuth()
                .username("yourUsername")
                .password("yourPassword".getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .buildAsync();
    }


}