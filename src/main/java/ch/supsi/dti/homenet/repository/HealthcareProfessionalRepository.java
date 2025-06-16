package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthcareProfessionalRepository extends JpaRepository<HealthcareProfessional, Long> {
    Optional<HealthcareProfessional> findByUsername(String username);
    List<HealthcareProfessional> findByRole(Role role);

}
