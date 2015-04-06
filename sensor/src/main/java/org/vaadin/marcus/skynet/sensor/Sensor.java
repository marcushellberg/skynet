package org.vaadin.marcus.skynet.sensor;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.vaadin.marcus.skynet.shared.MQTTHelper;
import org.vaadin.marcus.skynet.shared.Skynet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Sensor {

    private static String TOPIC = Skynet.TOPIC_SENSORS + "/temperature/indoor";
    public static final int MEASURE_INTERVAL = 1000;

    private Path sensorPath;
    private MqttClient client;

    public static void main(String[] args) throws Exception {
        new Sensor().start();
    }

    public Sensor() throws Exception {
        setupSensor();
        client = MQTTHelper.connect(TOPIC);
    }

    private void start() throws Exception {
        reportSensorData();
    }

    private void setupSensor() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("modprobe w1-gpio").waitFor();
        runtime.exec("modprobe w1-therm").waitFor();
        String basedir = "/sys/bus/w1/devices";
        for (File f : new File(basedir).listFiles()) {
            if (f.getName().startsWith("28")) {
                sensorPath = Paths.get(basedir + "/" + f.getName() + "/w1_slave");
                return;
            }
        }
        throw new Exception("No temp sensors found!");
    }


    private void reportSensorData() throws MqttException, InterruptedException, IOException {
        while (true) {
            publishMessage("time=" + System.currentTimeMillis() + ",temp=" + readTemp());
            Thread.sleep(MEASURE_INTERVAL);
        }
    }

    private void publishMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(0);
        message.setRetained(false);
        client.publish(TOPIC, message);
        System.out.println(TOPIC + " " +message);
    }

    private float readTemp() throws IOException, InterruptedException {
        List<String> sensorInput = Files.readAllLines(sensorPath);
        while (!sensorInput.get(0).contains("YES")) {
            Thread.sleep(200);
            sensorInput = Files.readAllLines(sensorPath);
        }
        float temp = new Float(sensorInput.get(1).split("t=")[1]) / 1000;
        return temp;

    }
}
