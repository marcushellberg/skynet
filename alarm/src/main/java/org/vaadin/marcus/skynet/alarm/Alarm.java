package org.vaadin.marcus.skynet.alarm;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.vaadin.marcus.skynet.shared.MQTTHelper;
import org.vaadin.marcus.skynet.shared.Skynet;

public class Alarm implements MqttCallback {

    public static final String TOPIC = Skynet.TOPIC_ALARMS + "/visual/blinky";
    private final GpioPinDigitalOutput pin;
    private MqttClient client;

    public static void main(String[] args) throws Exception {
        new Alarm().start();
    }

    private void start() throws InterruptedException {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public Alarm() throws Exception {
        client = MQTTHelper.connect(TOPIC);
        client.setCallback(this);
        client.subscribe(Skynet.TOPIC_ALARMS + "/#", 0);

        pin = GpioFactory.getInstance().provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
    }

    public void connectionLost(Throwable throwable) {

    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        if (message.contains(Skynet.HELLO)) {
            MqttMessage onlineResponse = new MqttMessage(Skynet.ONLINE.getBytes());
            onlineResponse.setQos(1);
            client.publish(TOPIC, onlineResponse);
        } else if (topic.equals(TOPIC)) {
            showAlert(Skynet.Alert.valueOf(message));
        }
    }

    private void showAlert(Skynet.Alert alert) throws InterruptedException {
        int sets, reps, speed;
        switch (alert) {
            case INFO:
                sets = 1;
                reps = 2;
                speed = 1000;
                break;
            case WARNING:
                sets = 2;
                reps = 2;
                speed = 200;
                break;
            case SEVERE:
            default:
                sets = 10;
                reps = 3;
                speed = 100;
        }

        for (int i = 0; i < sets; i++) {
            for (int j = 0; j < reps; j++) {
                pin.high();
                Thread.sleep(speed);
                pin.low();
                Thread.sleep(speed);
            }
            Thread.sleep(1000);
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
