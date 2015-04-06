package org.vaadin.marcus.skynet.service;

import org.vaadin.marcus.skynet.entities.Sensor;

public class SensorOfflineEvent {
    private Sensor sensor;

    public SensorOfflineEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
