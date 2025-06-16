package ch.supsi.dti.homenet.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "patient")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //need to be unique
    @Column(nullable = false, unique = true)
    private String uuid;

    @Column
    private String name;
    @Column
    private String surname;
    @Column
    private String sex;
    @Column
    private String diagnosisDesc;
    @Column
    private String idGarmin;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "id_category", nullable = false)
    private DiseaseCategory diseaseCategory;

    @ManyToMany
    @JoinTable(
            name = "patient_healthcare_professional",
            joinColumns = @JoinColumn(name = "id_patient"),
            inverseJoinColumns = @JoinColumn(name = "id_healthcare_professional")
    )
    private Set<HealthcareProfessional> healthcareProfessionals;

    @Column
    private String voucher;
    @Getter
    @Column(name = "temporary_token")
    private String temporaryToken;
    @Getter
    @Column(name = "temporary_token_secret")
    private String temporaryTokenSecret;
    @Getter
    @Column
    private String token;
    @Getter
    @Column
    private String tokenSecret;
    @Getter
    @Column
    private String verifier;
    @Getter
    @Column(name = "activity_export_permission")
    private Boolean activityExportPermission = false;
    @Getter
    @Column(name = "health_export_permission")
    private Boolean healthExportPermission = false;
    @Column(name = "last_sync")
    private LocalDateTime lastSync;
    @Column(name = "first_sync")
    private LocalDateTime firstSync;

    private static final String AUTHORIZATION_LINK = "https://homenet.dti.supsi.ch/connect/auth/";

    public User setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public User setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
        return this;
    }

    public User setTemporaryTokenSecret(String temporaryTokenSecret) {
        this.temporaryTokenSecret = temporaryTokenSecret;
        return this;
    }

    public User setToken(String token) {
        this.token = token;
        return this;
    }

    public User setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
        return this;
    }

    public User setVerifier(String verifier) {
        this.verifier = verifier;
        return this;
    }

    public User setActivityExportPermission(Boolean activityExportPermission) {
        this.activityExportPermission = activityExportPermission;
        return this;
    }

    public User setHealthExportPermission(Boolean healthExportPermission) {
        this.healthExportPermission = healthExportPermission;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", voucher='" + voucher + '\'' +
                ", temporaryToken='" + temporaryToken + '\'' +
                ", temporaryTokenSecret='" + temporaryTokenSecret + '\'' +
                ", token='" + token + '\'' +
                ", tokenSecret='" + tokenSecret + '\'' +
                ", verifier='" + verifier + '\'' +
                ", activityExportPermission=" + activityExportPermission +
                ", healthExportPermission=" + healthExportPermission +
                ", lastSync=" + lastSync +
                ", firstSync=" + firstSync +
                '}';
    }

    public void addHealthcareProfessional(HealthcareProfessional professional) {
        healthcareProfessionals.add(professional);
    }

    public String getAuthorizationLink() {
        return AUTHORIZATION_LINK + uuid;
    }
}
