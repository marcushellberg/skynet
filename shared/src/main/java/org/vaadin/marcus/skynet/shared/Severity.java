package org.vaadin.marcus.skynet.shared;

public enum Severity {
    INFO("info"), WARNING("warning"), SEVERE("severe");

    private String level;

    Severity(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
