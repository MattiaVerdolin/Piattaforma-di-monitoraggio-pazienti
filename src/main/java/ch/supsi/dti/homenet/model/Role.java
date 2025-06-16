package ch.supsi.dti.homenet.model;
import lombok.Getter;

@Getter
public enum Role {
    COORD("Coordinatore"),
    INF("Infermiere"),
    FISIO("Fisioterapista"),
    MED_PAL("Medico cure palliative"),
    MED_PNEUM("Medico pneumologo"),
    MED_FAM("Medico di famiglia");

    private final String role;

    private Role(String role) {
        this.role = role;
    }
}
