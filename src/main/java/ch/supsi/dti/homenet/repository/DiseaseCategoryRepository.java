package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.DiseaseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseCategoryRepository extends JpaRepository<DiseaseCategory, Long> {
    Optional<DiseaseCategory> findByName(String aDefault);
}
