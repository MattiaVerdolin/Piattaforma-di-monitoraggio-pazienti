package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.Measurement;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    Optional<Measurement> findByPatientAndMeasurementTypeAndData(User patient, MeasurementType measurementType, Date data);
    Optional<Measurement> findByPatientAndMeasurementType(User patient, MeasurementType measurementType);
    Optional<Measurement> findTopByPatientAndMeasurementTypeOrderByDataDesc(User patient, MeasurementType type);

    @Query("SELECT m FROM Measurement m WHERE m.patient = :patient AND m.measurementType = :measurementType AND FUNCTION('DATE', m.data) = FUNCTION('DATE', :data)")
    Optional<Measurement> findByPatientAndMeasurementTypeAndDateDay(@Param("patient") User patient, @Param("measurementType") MeasurementType measurementType, @Param("data") Date data);

    @Query("SELECT m FROM Measurement m WHERE m.patient = :patient AND m.measurementType = :measurementType " +
            "AND FUNCTION('DATE', m.data) = FUNCTION('DATE', :data) ORDER BY m.data ASC")
    List<Measurement> findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(
            @Param("patient") User patient,
            @Param("measurementType") MeasurementType measurementType,
            @Param("data") Date data);

    @Query("SELECT m FROM Measurement m WHERE m.patient = :patient AND m.measurementType = :measurementType " +
            "AND FUNCTION('DATE', m.data) = FUNCTION('DATE', :data) ORDER BY m.data DESC")
    List<Measurement> findTopByPatientAndMeasurementTypeAndDateOrderByDataDesc(
            @Param("patient") User patient,
            @Param("measurementType") MeasurementType measurementType,
            @Param("data") Date data);

    void deleteByPatient(User patient);

}

