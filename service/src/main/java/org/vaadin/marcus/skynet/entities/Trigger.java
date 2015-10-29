package org.vaadin.marcus.skynet.entities;

import org.vaadin.marcus.skynet.shared.Severity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Trigger {

    public enum Condition {GREATER_THAN, LESS_THAN}

    private final UUID id;
    private Sensor sensor;
    private boolean triggered = false;

    private boolean triggerAll = true;
    private Set<Alarm> alarms = new HashSet<>();
    private Condition condition = Condition.GREATER_THAN;
    private Double triggerValue;
    private Severity severity = Severity.INFO;

    public Trigger() {
        id = UUID.randomUUID();
    }

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
    }

    public boolean isTriggerAll() {
        return triggerAll;
    }

    public void setTriggerAll(boolean triggerAll) {
        this.triggerAll = triggerAll;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Double getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(Double triggerValue) {
        this.triggerValue = triggerValue;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public boolean isTriggeredBy(Sensor sensor) {
        if (!triggered && this.sensor.equals(sensor)) {
            if (condition == Condition.GREATER_THAN) {
                return sensor.getValue() > triggerValue;
            } else if (condition == Condition.LESS_THAN) {
                return sensor.getValue() < triggerValue;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Trigger: " + condition + " " + triggerValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trigger trigger = (Trigger) o;

        return !(id != null ? !id.equals(trigger.id) : trigger.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
