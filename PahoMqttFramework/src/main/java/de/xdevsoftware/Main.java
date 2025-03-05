package de.xdevsoftware;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;


public class Main
{
    private static final Logger logger = LogManager.getLogger(Main.class);

    private static final String TCP_BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String TLS_BROKER_URL = "ssl://broker.emqx.io:8883";
    private static final String MQTT_MESSAGE = "Hello Devnexus! ^^";
    private static final String MQTT_TOPIC = "topic/testdevnexus";
    private static final String CLIENT_ID = "demo_client";

    // qos At least once (1): The message is delivered at least once. Duplicates are possible.
    // The message will be delivered to the subscriber in any case.
    private static final int QOS = 1;


    public static void main(String[] args)
    {
        MqttClient client = buildClient(TLS_BROKER_URL, CLIENT_ID);

        if (client != null && client.isConnected())
        {
            setupCallback(client);
            subscribeToTopic(client, MQTT_TOPIC, QOS);
            publishMessage(client, MQTT_TOPIC, MQTT_MESSAGE, QOS);
            disconnectClient(client);
        }
    }

    private static MqttClient buildClient(String broker, String clientId)
    {
        try
        {
            MqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            client.connect(options);
            logger.info("Connected to broker: {}", broker);
            return client;
        }
        catch (MqttException e)
        {
            logger.error("Failed to connect to broker: {}", e.getMessage());
            return null;
        }
    }

    private static MqttClient buildClientWithUsernamePassword(String broker, String clientId)
    {
        try
        {
            MqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("your_username");
            options.setPassword("your_password".toCharArray());
            client.connect(options);
            logger.info("Connected to broker: {} with Username/Password", broker);
            return client;
        }
        catch (MqttException e)
        {
            logger.error("Failed to connect to broker with Username/Password: {}", e.getMessage());
            return null;
        }
    }


    private static void subscribeToTopic(MqttClient client, String topic, int qos)
    {
        try
        {
            client.subscribe(topic, qos);
            logger.info("Subscribed to topic: {} with QoS: {}", topic, qos);
        }
        catch (MqttException e)
        {
            logger.error("Failed to subscribe to topic: {}", e.getMessage());
        }
    }

    private static void publishMessage(MqttClient client, String topic, String messageContent, int qos)
    {
        try
        {
            MqttMessage message = new MqttMessage(messageContent.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
            logger.info("Message published to topic: {} with QoS: {}", topic, qos);
        }
        catch (MqttException e)
        {
            logger.error("Failed to publish message: {}", e.getMessage());
        }
    }

    private static void setupCallback(MqttClient client)
    {
        client.setCallback(new MqttCallback()
        {
            @Override
            public void messageArrived(String topic, MqttMessage message)
            {
                logger.info("Message arrived - Topic: {}, QoS: {}, Content: {}", topic, message.getQos(), new String(message.getPayload()));
            }

            @Override
            public void connectionLost(Throwable cause)
            {
                logger.error("Connection lost: {}", cause.getMessage());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token)
            {
                logger.info("Delivery complete: {}", token.isComplete());
            }
        });
    }

    private static void disconnectClient(MqttClient client)
    {
        try
        {
            client.disconnect();
            client.close();
            logger.info("Disconnected from broker.");
        }
        catch (MqttException e)
        {
            logger.error("Failed to disconnect: {}", e.getMessage());
        }
    }
}