package ch.supsi.dti.homenet.enums;

import lombok.Getter;

@Getter
public enum SummaryTypes {
    DEREGISTRATION("deregistration"),
    DAILIES("dailies"),
    PULSEOX("pulseox"),
    STRESS_DETAILS("stressDetails");

    private final String key;

    SummaryTypes(String key) {
        this.key = key;
    }
}
