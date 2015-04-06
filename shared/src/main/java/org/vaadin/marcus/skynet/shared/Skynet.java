package org.vaadin.marcus.skynet.shared;

public class Skynet {
    public static final String BROKER = "tcp://test.mosquitto.org";
    public static final String TOPIC = "v-skynet";
    public static final String TOPIC_SENSORS = TOPIC + "/sensors";
    public static final String TOPIC_ALARMS = TOPIC + "/alarms";

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String HELLO = "hello?";


    public enum Alert {
        INFO("info"), WARNING("warning"), SEVERE("severe");

        private String level;

        Alert(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }
    }
}

