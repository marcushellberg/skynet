package org.vaadin.marcus.skynet.service;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.vaadin.marcus.skynet.entities.Alarm;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.shared.Skynet;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageService implements MqttCallback {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Set<Alarm> alarms = new HashSet<>();
    private Set<Sensor> sensors = new HashSet<>();
    private Set<Trigger> triggers = new HashSet<>();
    private MqttClient client;

    @Inject
    private Event<SensorUpdatedEvent> sensorUpdatedEvent;
    @Inject
    private Event<SensorOfflineEvent> sensorOfflineEvent;


    public void init() throws MqttException {
        client = new MqttClient(Skynet.BROKER, MqttClient.generateClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect();

        client.subscribe(Skynet.TOPIC_ALARMS + "/#");
        client.subscribe(Skynet.TOPIC_SENSORS + "/#");
        client.setCallback(this);
    }

    private void handleSensorMessage(String topic, MqttMessage message) {
        String[] typeAndName = topic.replaceAll(Skynet.TOPIC_SENSORS, "").split("/");
        Sensor sensor = new Sensor(typeAndName[0], typeAndName[1]);

        String content = new String(message.getPayload());

        if (content.contains(Skynet.OFFLINE)) {
            sensors.remove(sensor);
            sensorOfflineEvent.fire(new SensorOfflineEvent(sensor));
        } else {
            String[] data = content.split(",");
            Date time = new Date(new Long(data[0].replaceAll("time=", "")));
            Float temp = new Float(data[1].replaceAll("temp=", ""));

            sensor.setTime(time);
            sensor.setValue(temp);

            System.out.println(sensor.getName() + ": " + sensor.getValue());
            sensors.add(sensor);
            sensorUpdatedEvent.fire(new SensorUpdatedEvent(sensor));
        }

    }

    private void handleAlarmMessage(String topic, MqttMessage message) {
        // We're only interested in discovering new alarms and clearing out disconnected ones
        String[] typeAndName = topic.replaceAll(Skynet.TOPIC_ALARMS, "").split("/");
        Alarm alarm = new Alarm(typeAndName[0], typeAndName[1]);

        if (topic.contains(Skynet.ONLINE)) {
            alarms.add(alarm);
        } else if (topic.contains(Skynet.OFFLINE)) {
            alarms.remove(alarm);
        }

    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Lost connection..");
        throwable.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (topic.startsWith(Skynet.TOPIC_SENSORS)) {
            handleSensorMessage(topic, message);
        } else if (topic.startsWith(Skynet.TOPIC_ALARMS)) {
            handleAlarmMessage(topic, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
