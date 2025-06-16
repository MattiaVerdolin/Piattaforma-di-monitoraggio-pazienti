package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.InterventionDTO;
import ch.supsi.dti.homenet.model.MedicalIntervention;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MedicalInterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicalInterventionService {

    private final MedicalInterventionRepository medicalInterventionRepository;

    /**
     * Find all interventions for a patient
     * @param patient The patient to search for
     * @return List of medical interventions
     */
    public List<MedicalIntervention> findByPatient(User patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        return medicalInterventionRepository.findByPatient(patient);
    }

    /**
     * Find an intervention by its ID
     * @param id The ID of the intervention
     * @return Optional containing the intervention if found
     */
    public Optional<MedicalIntervention> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return medicalInterventionRepository.findById(id);
    }

    public void save(MedicalIntervention medicalIntervention) {
        medicalInterventionRepository.save(medicalIntervention);
    }

    public void deleteById(Long id) {
        medicalInterventionRepository.deleteById(id);
    }

    public MedicalIntervention updateIntervention (InterventionDTO dto) {
        MedicalIntervention medicalIntervention = medicalInterventionRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Intervento non trovato"));

        medicalIntervention.setTitle(dto.getTitle());
        medicalIntervention.setDescription(dto.getDescription());

        return medicalInterventionRepository.save(medicalIntervention);
    }

}