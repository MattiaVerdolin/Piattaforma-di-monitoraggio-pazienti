package ch.supsi.dti.homenet.enums;

import lombok.Getter;

@Getter
public enum JsonKeys {
    STEPS("steps"),

    //Heart Rate
    TIME_OFFSET_HEART_RATE_SAMPLES("timeOffsetHeartRateSamples"),
    HEART_RATE("heartRate"),
    MIN_HEART_RATE("minHeartRateInBeatsPerMinute"),
    MAX_HEART_RATE("maxHeartRateInBeatsPerMinute"),
    AVERAGE_HEART_RATE("averageHeartRateInBeatsPerMinute"),

    //Pulse OX
    TIME_OFFSET_SPO2_VALUES("timeOffsetSpo2Values"),
    SPO2("spo2"),
    MIN_SPO2("minSpo2"),
    MAX_SPO2("maxSpo2"),
    AVERAGE_SPO2("averageSpo2"),

    //Stress details
    TIME_OFFSET_STRESS_LEVEL_VALUES("timeOffsetStressLevelValues"),
    STRESS("stress"),
    AVERAGE_STRESS_LEVEL("averageStressLevel"),
    MAX_STRESS_LEVEL("maxStressLevel"),

    //Other functional keys
    USER_ACCESS_TOKEN("userAccessToken"),
    START_TIME_IN_SECONDS("startTimeInSeconds"),
    START_TIME_OFFSET_IN_SECONDS("startTimeOffsetInSeconds"),
    CALENDAR_DATE("calendarDate");

    private final String key;

    JsonKeys(String key) {
        this.key = key;
    }

}
