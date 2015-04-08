package org.vaadin.marcus.skynet.alarm;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.eclipse.paho.client.mqttv3.*;
import org.vaadin.marcus.skynet.shared.MQTTHelper;
import org.vaadin.marcus.skynet.shared.Severity;
import org.vaadin.marcus.skynet.shared.Skynet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Alarm implements MqttCallback {

    public static final String TOPIC = Skynet.TOPIC_ALARMS + "/visual/blinky";
    ExecutorService alarmService = Executors.newFixedThreadPool(1);
    private final GpioPinDigitalOutput pin;
    private MqttClient client;

    public static void main(String[] args) throws Exception {
        new Alarm();
    }


    public Alarm() throws Exception {
        client = MQTTHelper.connect(TOPIC);
        client.setCallback(this);
        client.subscribe(Skynet.TOPIC_ALARMS + "/#", 0);
        pin = GpioFactory.getInstance().provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
        alarmService.execute(new Alert(Severity.WARNING.getLevel()));
        announce();
    }

    public void connectionLost(Throwable throwable) {
        throwable.printStackTrace(System.out);
        System.out.println("Connection was lost :(");
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println("Got message: " + topic + " - " + message);
        if (message.contains(Skynet.HELLO)) {
            announce();
        }
        if (message.contains(Skynet.OFFLINE) || message.contains(Skynet.ONLINE)) {
            // This client does not currently care about other alarms
        } else if (topic.equals(TOPIC)) {
            alarmService.execute(new Alert(message));
        }
    }

    private void announce() throws MqttException {
        MqttMessage onlineResponse = new MqttMessage(Skynet.ONLINE.getBytes());
        onlineResponse.setQos(1);
        client.publish(TOPIC, onlineResponse);
    }

    class Alert implements Runnable {
        private String severity;

        Alert(String severity) {
            this.severity = severity;
        }

        @Override
        public void run() {
            int sets, reps, speed;

            if (Severity.INFO.getLevel().equals(severity)) {
                sets = 2;
                reps = 2;
                speed = 1000;
            } else if (Severity.WARNING.getLevel().equals(severity)) {
                sets = 4;
                reps = 2;
                speed = 200;
            } else if (Severity.SEVERE.getLevel().equals(severity)) {
                sets = 10;
                reps = 3;
                speed = 100;
            } else {
                System.out.println("Tried to trigger alarm with unknown level '" + severity + "'");
                return;
            }
            System.out.println("Alarm: " + severity);
            try {
                for (int i = 0; i < sets; i++) {
                    for (int j = 0; j < reps; j++) {
                        pin.high();
                        Thread.sleep(speed);
                        pin.low();
                        Thread.sleep(speed);
                    }
                    Thread.sleep(1000);
                }
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
