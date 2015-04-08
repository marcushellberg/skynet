package org.vaadin.marcus.skynet.service;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vaadin.marcus.skynet.entities.Alarm;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.shared.Skynet;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class MessageServiceTest {

    public static final String SENSOR_TOPIC = Skynet.TOPIC_SENSORS + "/temp/test";
    public static final String ALARM_TOPIC = Skynet.TOPIC_ALARMS + "/visual/test";

    @Mock
    EventBus eventBus;
    @Mock
    MqttClient client;

    private MessageService messageService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        messageService = new MessageService();
        messageService.eventBus = eventBus;
        messageService.client = client;
    }

    @Test
    public void testSensorDetection() {
        receiveSensorMessage();
        assertEquals("Sensor did not get saved", 1, messageService.sensors.size());
    }

    @Test
    public void testSensorTempParsing() {
        Float temp = receiveSensorMessage();
        assertEquals("Sensor value was incorrect", temp, getFirstSensor().getValue(), 0.0001);
    }

    @Test
    public void testSensorNameDetection() {
        receiveSensorMessage();
        assertEquals("Sensor name was incorrect", "test", getFirstSensor().getName());
    }

    @Test
    public void testSensorTypeDetection() {
        receiveSensorMessage();
        assertEquals("Sensor type was incorrect", "temp", getFirstSensor().getType());
    }


    @Test
    public void testSensorValueEvent() {
        receiveSensorMessage();

        verify(eventBus).post(new SensorUpdatedEvent(new Sensor("temp", "test")));
    }


    @Test
    public void testSensorOfflineEvent() {
        receiveSensorMessage();
        messageService.handleSensorMessage(SENSOR_TOPIC, new MqttMessage(Skynet.OFFLINE.getBytes()));

        assertEquals("Offline sensor was not removed from memory", 0, messageService.sensors.size());
    }

    @Test
    public void testAlarmOnlineEvent() {
        receiveAlarmMessage();

        assertEquals("Alarm was not saved", 1, messageService.alarms.size());
    }


    @Test
    public void testAlarmTypeDetection() {
        receiveAlarmMessage();

        assertEquals("Alarm type was incorrect", "visual", getFirstAlarm().getType());
    }

    @Test
    public void testAlarmNameDetection() {
        receiveAlarmMessage();

        assertEquals("Alarm name was incorrect", "test", getFirstAlarm().getName());
    }

    @Test
    public void testAlarmOfflineEvent() {
        messageService.handleAlarmMessage(ALARM_TOPIC, new MqttMessage(Skynet.OFFLINE.getBytes()));

        assertEquals("Alarm was not cleaned up on disconnect", 0, messageService.alarms.size());
    }

    @Test
    public void testTrigger() {
        Trigger trigger = new Trigger();
        trigger.setCondition(Trigger.Condition.LARGER_THAN);
        trigger.setTriggerValue(new Float(10.0));
        Sensor sensor = new Sensor("temp", "test");
        trigger.setSensor(sensor);
        messageService.addTrigger(trigger);

        receiveSensorMessage();

        verify(eventBus).post(new SensorTriggeredEvent(sensor, trigger));
    }

    @Test
    public void testThatTriggerSensAlarms() throws MqttException {
        Trigger trigger = new Trigger();
        trigger.setCondition(Trigger.Condition.LARGER_THAN);
        trigger.setTriggerValue(new Float(10.0));
        Sensor sensor = new Sensor("temp", "test");
        trigger.setSensor(sensor);
        Set<Alarm> alarms = Sets.newHashSet();
        alarms.add(new Alarm("visual", "test"));
        trigger.setAlarms(alarms);
        messageService.addTrigger(trigger);

        receiveSensorMessage();

        verify(client).publish(eq(ALARM_TOPIC), any(MqttMessage.class));
    }

    private Float receiveSensorMessage() {
        Float temp = new Float(20.51);
        messageService.handleSensorMessage(SENSOR_TOPIC, new MqttMessage(("time=120391209310293,temp=" + temp).getBytes()));
        return temp;
    }

    private Sensor getFirstSensor() {
        return messageService.sensors.iterator().next();
    }

    private void receiveAlarmMessage() {
        messageService.handleAlarmMessage(ALARM_TOPIC, new MqttMessage(Skynet.ONLINE.getBytes()));
    }

    private Alarm getFirstAlarm() {
        return messageService.alarms.iterator().next();
    }
}
