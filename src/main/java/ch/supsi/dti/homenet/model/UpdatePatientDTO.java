package ch.supsi.dti.homenet.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdatePatientDTO {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String sex;
    private String diagnosisDesc;
    private Long diseaseCategoryId;
    private Long medicoBaseId;
    private Long medicoPneumoId;
    private Long fisioterapistaId;
    private Long medicoPalId;
    private Double minHr;
    private Double maxHr;
    private Double minSaturation;

}
