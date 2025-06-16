package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.AlertMargin;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertMarginRepository extends JpaRepository<AlertMargin, Long> {
    Optional<AlertMargin> findByPatientAndMeasurementType(User patient, MeasurementType type);
    List<AlertMargin> findAllByPatient(User patient);

    @Modifying
    @Query("DELETE FROM AlertMargin am WHERE am.patient = :patient")
    void deleteByPatient(@Param("patient") User patient);

}


