package ch.supsi.dti.homenet.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "alert_margin")
@Getter @Setter
public class AlertMargin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "id_measurement_type", nullable = false)
    private MeasurementType measurementType;

    @Column
    private Double min;

    @Column
    private Double max;
}