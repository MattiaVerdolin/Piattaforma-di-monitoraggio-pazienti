package ch.supsi.dti.homenet.model;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import jakarta.persistence.*;

@Entity
@Table(name = "measurement_type")
@Getter
@Setter
public class MeasurementType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}