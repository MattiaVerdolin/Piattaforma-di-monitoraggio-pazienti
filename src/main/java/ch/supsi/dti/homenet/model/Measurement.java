package ch.supsi.dti.homenet.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Table(name = "measurement")
@Getter @Setter
public class Measurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "id_measurement_type", nullable = false)
    private MeasurementType measurementType;

    private Date data;

    private Double value;
    private Boolean isDaily;
}
