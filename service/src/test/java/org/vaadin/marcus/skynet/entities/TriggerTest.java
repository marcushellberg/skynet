package org.vaadin.marcus.skynet.entities;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TriggerTest {

    private Trigger trigger;

    @Before
    public void setup() {
        trigger = new Trigger();
        trigger.setTriggerValue(new Double(30.0));
    }


    @Test
    public void testTriggerForLargerValue() {
        Sensor sensor = getSensor(Trigger.Condition.GREATER_THAN, new Double(100.0));

        assertTrue("A value more than the trigger value did not get triggered", trigger.isTriggeredBy(sensor));
    }


    @Test
    public void testTriggerForSmallerValue() {
        Sensor sensor = getSensor(Trigger.Condition.LESS_THAN, new Double(10.0));

        assertTrue("A value less than the trigger value did not get triggered", trigger.isTriggeredBy(sensor));
    }

    @Test
    public void testNoTrigger() {
        Sensor sensor = getSensor(Trigger.Condition.GREATER_THAN, new Double(10.0));

        assertFalse("A value less than the trigger value got triggered", trigger.isTriggeredBy(sensor));
    }


    private Sensor getSensor(Trigger.Condition largerThan, Double value) {
        trigger.setCondition(largerThan);
        Sensor sensor = new Sensor("test", "test");
        sensor.setValue(value);
        trigger.setSensor(sensor);
        return sensor;
    }
}
