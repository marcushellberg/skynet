package org.vaadin.marcus.skynet.events;

import org.vaadin.marcus.skynet.entities.Sensor;

public class SensorUpdatedEvent {
    private Sensor sensor;

    public SensorUpdatedEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorUpdatedEvent that = (SensorUpdatedEvent) o;

        return !(sensor != null ? !sensor.equals(that.sensor) : that.sensor != null);

    }

    @Override
    public int hashCode() {
        return sensor != null ? sensor.hashCode() : 0;
    }
}
