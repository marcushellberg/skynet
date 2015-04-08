package org.vaadin.marcus.skynet.service;

import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;

public class SensorTriggeredEvent {
    private final Sensor sensor;
    private final Trigger trigger;

    public SensorTriggeredEvent(Sensor sensor, Trigger trigger) {
        this.sensor = sensor;
        this.trigger = trigger;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorTriggeredEvent that = (SensorTriggeredEvent) o;

        if (sensor != null ? !sensor.equals(that.sensor) : that.sensor != null) return false;
        return !(trigger != null ? !trigger.equals(that.trigger) : that.trigger != null);

    }

    @Override
    public int hashCode() {
        int result = sensor != null ? sensor.hashCode() : 0;
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        return result;
    }
}
