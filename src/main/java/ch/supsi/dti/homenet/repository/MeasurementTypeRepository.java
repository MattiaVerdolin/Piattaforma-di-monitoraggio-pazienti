package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementTypeRepository extends JpaRepository<MeasurementType, Long> {
    MeasurementType findByName(String type);
}
