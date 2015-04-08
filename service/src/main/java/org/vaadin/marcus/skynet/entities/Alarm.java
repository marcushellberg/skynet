package org.vaadin.marcus.skynet.entities;

public class Alarm {

    private final String type;
    private final String name;

    public Alarm(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alarm alarm = (Alarm) o;

        if (!type.equals(alarm.type)) return false;
        return name.equals(alarm.name);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getTopic() {
        return "/" + type + "/" + name;
    }
}
