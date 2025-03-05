package de.xdevsoftware;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main
{
    private static final Logger logger = LogManager.getLogger(Main.class);

    private static final String MQTT_BROKER_URL = "broker.emqx.io";
    private static final int TCP_PORT = 1883;
    private static final int TLS_PORT = 8883;
    private static final String MQTT_MESSAGE = "hello devnexus!";
    private static final String MQTT_TOPIC = "topic/testdevnexus";

    public static void main(String[] args)
    {
        Vertx vertx = Vertx.vertx();
        MqttClient client = createClientTLS(vertx);

        connectToBroker(client, TLS_PORT, MQTT_BROKER_URL);
    }

    private static MqttClient createClientTCP(Vertx vertx)
    {
        MqttClientOptions options = new MqttClientOptions();
        return MqttClient.create(vertx, options);
    }

    private static MqttClient createClientUsernamePassword(Vertx vertx, String username, String password)
    {
        MqttClientOptions options = new MqttClientOptions()
                .setUsername(username)
                .setPassword(password);
        return MqttClient.create(vertx, options);
    }

    private static MqttClient createClientTLS(Vertx vertx)
    {
        MqttClientOptions options = new MqttClientOptions()
                .setSsl(true)
                .setHostnameVerificationAlgorithm("HTTPS");
        return MqttClient.create(vertx, options);
    }

    private static void connectToBroker(MqttClient client, int port, String broker)
    {
        client.connect(port, broker, connectionResult ->
        {
            if (connectionResult.succeeded())
            {
                logger.info("Connected to MQTT Broker");
                subscribeToTopic(client, MQTT_TOPIC);
            }
            else
            {
                logger.error("Failed to connect to MQTT Broker");
            }
        });
    }

    private static void subscribeToTopic(MqttClient client, String topic)
    {
        client.subscribe(topic, 0, subscriptionResult ->
        {
            if (subscriptionResult.succeeded())
            {
                logger.info("Subscribed to topic successfully");
                setupCallback(client);
                publishMessage(client, topic, MQTT_MESSAGE);
            }
            else
            {
                logger.error("Failed to subscribe to topic");
            }
        });
    }

    private static void setupCallback(MqttClient client)
    {
        client.publishHandler(message ->
        {
            logger.info("Received message on topic: {}", message.topicName());
            logger.info("Message payload: {}", message.payload().toString());
        });
    }

    // qos EXACTLY ONCE (2): The message is delivered exactly once. Without duplicates.
    private static void publishMessage(MqttClient client, String topic, String messageContent)
    {
        client.publish(
                topic,
                Buffer.buffer(messageContent),
                MqttQoS.EXACTLY_ONCE,
                false,
                false,
                publishResult ->
                {
                    if (publishResult.succeeded())
                    {
                        logger.info("Message published successfully to topic: {}", topic);
                    }
                    else
                    {
                        logger.error("Failed to publish message to topic: {}", topic);
                    }
                }
        );
    }
}
