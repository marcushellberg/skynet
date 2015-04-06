package org.vaadin.marcus.skynet.shared;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTHelper {

    public static MqttClient connect(String topic) throws MqttException {
        MqttClient client = new MqttClient(Skynet.BROKER, MqttClient.generateClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setWill(topic, Skynet.OFFLINE.getBytes(), 1, true);
        client.connect();
        return client;
    }
}
