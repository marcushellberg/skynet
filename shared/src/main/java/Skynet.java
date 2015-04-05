
public class Skynet {
    public static final String TOPIC = "v-skynet";
    public static final String TOPIC_SENSORS = TOPIC + "/sensors";
    public static final String TOPIC_ALARMS = TOPIC + "/alarms";

    public enum Alert {
        INFO("info"), WARN("warn"), SEVERE("severe");

        private String level;

        Alert(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }
    }
}

