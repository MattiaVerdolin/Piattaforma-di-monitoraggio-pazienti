package ch.supsi.dti.homenet.model;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Table(name = "medical_intervention")
@Getter @Setter
public class MedicalIntervention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "id_healthcare_professional", nullable = false)
    private HealthcareProfessional healthcareProfessional;
}