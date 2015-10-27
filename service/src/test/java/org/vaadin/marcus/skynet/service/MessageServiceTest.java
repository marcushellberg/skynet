package org.vaadin.marcus.skynet.service;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vaadin.marcus.skynet.entities.Alarm;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.events.SensorOfflineEvent;
import org.vaadin.marcus.skynet.events.SensorTriggeredEvent;
import org.vaadin.marcus.skynet.events.SensorUpdatedEvent;
import org.vaadin.marcus.skynet.shared.Skynet;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageServiceTest {

    public static final String SENSOR_TOPIC = Skynet.TOPIC_SENSORS + "/temp/test";
    public static final String ALARM_TOPIC = Skynet.TOPIC_ALARMS + "/visual/test";

    @Mock
    EventBus eventBus;
    @Mock
    MqttAsyncClient client;

    private MessageService messageService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        messageService = MessageService.getInstance();
        messageService.eventBus = eventBus;
        messageService.client = client;
    }

    @Test
    public void testSensorDetection() {
        receiveSensorMessage();
        verify(eventBus).post(any(SensorUpdatedEvent.class));
    }


    @Test
    public void testSensorValueEvent() {
        receiveSensorMessage();

        verify(eventBus).post(new SensorUpdatedEvent(new Sensor("temp", "test")));
    }


    @Test
    public void testSensorTempParsing() {
        Double temp = receiveSensorMessage();
        ArgumentCaptor<SensorUpdatedEvent> event = ArgumentCaptor.forClass(SensorUpdatedEvent.class);
        verify(eventBus).post(event.capture());

        assertEquals("Sensor value was incorrect", temp, event.getValue().getSensor().getValue(), 0.0001);
    }

    @Test
    public void testSensorNameDetection() {
        receiveSensorMessage();
        ArgumentCaptor<SensorUpdatedEvent> event = ArgumentCaptor.forClass(SensorUpdatedEvent.class);
        verify(eventBus).post(event.capture());
        assertEquals("Sensor name was incorrect", "test", event.getValue().getSensor().getName());
    }

    @Test
    public void testSensorTypeDetection() {
        receiveSensorMessage();
        ArgumentCaptor<SensorUpdatedEvent> event = ArgumentCaptor.forClass(SensorUpdatedEvent.class);
        verify(eventBus).post(event.capture());

        assertEquals("Sensor type was incorrect", "temp", event.getValue().getSensor().getType());
    }


    @Test
    public void testSensorOfflineEvent() {
        messageService.handleSensorMessage(SENSOR_TOPIC, new MqttMessage(Skynet.OFFLINE.getBytes()));
        verify(eventBus).post(any(SensorOfflineEvent.class));
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
        trigger.setCondition(Trigger.Condition.GREATER_THAN);
        trigger.setTriggerValue(new Double(10.0));
        Sensor sensor = new Sensor("temp", "test");
        trigger.setSensor(sensor);
        messageService.addTrigger(trigger);

        receiveSensorMessage();

        verify(eventBus).post(new SensorTriggeredEvent(sensor, trigger));
    }

    @Test
    public void testThatTriggerSensAlarms() throws MqttException {
        IMqttDeliveryToken token = mock(IMqttDeliveryToken.class);
        when(client.publish(any(), any())).thenReturn(token);
        Trigger trigger = new Trigger();
        trigger.setCondition(Trigger.Condition.GREATER_THAN);
        trigger.setTriggerValue(new Double(10.0));
        Sensor sensor = new Sensor("temp", "test");
        trigger.setSensor(sensor);
        Set<Alarm> alarms = Sets.newHashSet();
        alarms.add(new Alarm("visual", "test"));
        trigger.setAlarms(alarms);
        messageService.addTrigger(trigger);

        receiveSensorMessage();

        verify(client).publish(eq(ALARM_TOPIC), any(MqttMessage.class));
    }

    private Double receiveSensorMessage() {
        Double temp = new Double(20.51);
        messageService.handleSensorMessage(SENSOR_TOPIC, new MqttMessage(("time=120391209310293,temp=" + temp).getBytes()));
        return temp;
    }


    private void receiveAlarmMessage() {
        messageService.handleAlarmMessage(ALARM_TOPIC, new MqttMessage(Skynet.ONLINE.getBytes()));
    }

    private Alarm getFirstAlarm() {
        return messageService.alarms.iterator().next();
    }
}
