package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.MedicalIntervention;
import ch.supsi.dti.homenet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface MedicalInterventionRepository extends JpaRepository<MedicalIntervention, Long> {
    List<MedicalIntervention> findByPatient(User patient);
}