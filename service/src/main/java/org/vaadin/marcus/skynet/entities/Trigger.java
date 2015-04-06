package org.vaadin.marcus.skynet.entities;

import java.util.HashSet;
import java.util.Set;

public class Trigger {

    private Sensor sensor;
    private Set<Alarm> alarms = new HashSet<>();;
    private boolean triggerAll = false;

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Set<Alarm> getAlarms() {
        return alarms;
    }

    public void setAlarms(Set<Alarm> alarms) {
        this.alarms = alarms;
        this.triggerAll = false;
    }

    public boolean isTriggerAll() {
        return triggerAll;
    }

    public void setTriggerAll(boolean triggerAll) {
        this.triggerAll = triggerAll;
        this.alarms = new HashSet<>();
    }


}
