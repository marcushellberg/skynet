package org.vaadin.marcus.skynet.service;

import org.vaadin.marcus.skynet.entities.Sensor;

public class SensorUpdatedEvent {
    private Sensor sensor;

    public SensorUpdatedEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
