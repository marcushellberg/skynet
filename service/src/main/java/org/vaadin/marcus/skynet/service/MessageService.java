package org.vaadin.marcus.skynet.service;

import com.google.common.eventbus.EventBus;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.vaadin.marcus.skynet.entities.Alarm;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.events.SensorOfflineEvent;
import org.vaadin.marcus.skynet.events.SensorTriggeredEvent;
import org.vaadin.marcus.skynet.events.SensorUpdatedEvent;
import org.vaadin.marcus.skynet.shared.Skynet;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageService implements MqttCallback {

    private static MessageService instance;
    protected Set<Alarm> alarms = new HashSet<>();
    protected Set<Sensor> sensors = new HashSet<>();
    protected Set<Trigger> triggers = new HashSet<>();
    protected MqttAsyncClient client;
    protected EventBus eventBus = new EventBus();

    private MessageService() {
    }

    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    public Set<Trigger> getTriggersForSensor(Sensor sensor) {
        return getTriggers().stream().filter(trigger -> trigger.getSensor().equals(sensor)).collect(Collectors.toSet());
    }

    public synchronized void addTrigger(Trigger trigger) {
        triggers.add(trigger);
    }

    public synchronized void removeTrigger(Trigger trigger) {
        triggers.remove(trigger);
    }

    public Set<Trigger> getTriggers() {
        return Collections.unmodifiableSet(triggers);
    }

    public Set<Alarm> getAlarms() {
        return Collections.unmodifiableSet(alarms);
    }


    public void registerListener(Object listener) {
        // Delay MQTT connection until somebody is actually listening
        if (client == null) {
            connect();
        }

        eventBus.register(listener);
    }

    public void unregisterListener(Object listener) {
        eventBus.unregister(listener);
    }


    protected void connect() {
        try {
            client = new MqttAsyncClient(Skynet.BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect().waitForCompletion();

            client.subscribe(Skynet.TOPIC_ALARMS + "/#", 0).waitForCompletion();
            client.subscribe(Skynet.TOPIC_SENSORS + "/#", 0).waitForCompletion();
            client.setCallback(this);

            client.publish(Skynet.TOPIC_ALARMS, new MqttMessage(Skynet.HELLO.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    protected void handleSensorMessage(String topic, MqttMessage message) {
        String[] typeAndName = topic.replaceAll(Skynet.TOPIC_SENSORS, "").split("/");
        Sensor sensor = new Sensor(typeAndName[1], typeAndName[2]);

        String content = new String(message.getPayload());

        if (content.contains(Skynet.OFFLINE)) {
            sensors.remove(sensor);
            postEvent(new SensorOfflineEvent(sensor));

        } else {
            String[] data = content.split(",");
            Date time = new Date(new Long(data[0].replaceAll("time=", "")));
            Double temp = new Double(data[1].replaceAll("temp=", ""));

            sensor.setTime(time);
            sensor.setValue(temp);

            sensors.add(sensor);
            postEvent(new SensorUpdatedEvent(sensor));

            getTriggers().forEach(trigger -> {
                if (trigger.isTriggeredBy(sensor)) {
                    triggerAlarm(sensor, trigger);
                }
            });
        }

    }

    protected void triggerAlarm(Sensor sensor, Trigger trigger) {
        trigger.setTriggered(true);

        postEvent(new SensorTriggeredEvent(sensor, trigger));

        Set<Alarm> alarms = trigger.getAlarms();
        if (trigger.isTriggerAll()) {
            alarms = getAlarms();
        }

        alarms.forEach(alarm -> {
            try {
                MqttMessage message = new MqttMessage(trigger.getSeverity().getLevel().getBytes());
                message.setQos(0);
                message.setRetained(false);
                String topic = Skynet.TOPIC_ALARMS + alarm.getTopic();
                IMqttDeliveryToken token = client.publish(topic, message);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        System.out.println("Send succeeded");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        exception.printStackTrace();
                    }
                });
                System.out.println("Triggered " + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    protected void handleAlarmMessage(String topic, MqttMessage message) {
        // We're only interested in discovering new alarms and clearing out disconnected ones
        System.out.println("Got alarm message with topic " + topic);
        if (topic.matches(Skynet.TOPIC_ALARMS + "/\\w+/\\w+")) {

            String[] typeAndName = topic.replaceAll(Skynet.TOPIC_ALARMS, "").split("/");
            Alarm alarm = new Alarm(typeAndName[1], typeAndName[2]);
            String content = new String(message.getPayload());

            if (content.contains(Skynet.ONLINE)) {
                alarms.add(alarm);
                System.out.println("Alarm " + alarm.getName() + " is ONLINE");
            } else if (content.contains(Skynet.OFFLINE)) {
                alarms.remove(alarm);

                // Clean up any triggers that may have had this alarm attached
                getTriggers().forEach(trigger -> {
                    if (trigger.getAlarms().contains(alarm)) {
                        trigger.getAlarms().remove(alarm);
                        if (trigger.getAlarms().isEmpty()) {
                            removeTrigger(trigger);
                        }
                    }
                });
                System.out.println("Alarm " + alarm.getName() + " is OFFLINE");
            }
        }
    }

    protected void postEvent(Object event) {
        try {
            eventBus.post(event);
        } catch (Exception ex) {
            // Make sure no UI exceptions bubble up into our service.
            // The Guava event bus runs the posts synchronously and any unhandled exceptions can kill the client.
            ex.printStackTrace();
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
