package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.*;
import ch.supsi.dti.homenet.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PatientService {

    private final DiseaseCategoryRepository diseaseCategoryRepository;
    private final HealthcareProfessionalRepository healthcareProfessionalRepository;
    private final UserRepository patientRepository;
    private final MedicalInterventionRepository medicalInterventionRepository;
    private final MeasurementRepository measurementRepository;
    private final AlertMarginService alertMarginService;

    public PatientService(DiseaseCategoryRepository diseaseCategoryRepository, HealthcareProfessionalRepository healthcareProfessionalRepository,
                          UserRepository patientRepository, MedicalInterventionRepository medicalInterventionRepository,
                          MeasurementRepository measurementRepository, AlertMarginService alertMarginService) {
        this.diseaseCategoryRepository = diseaseCategoryRepository;
        this.healthcareProfessionalRepository = healthcareProfessionalRepository;
        this.patientRepository = patientRepository;
        this.medicalInterventionRepository = medicalInterventionRepository;
        this.measurementRepository = measurementRepository;
        this.alertMarginService = alertMarginService;
    }

    public Optional<User> findPatientById(Long id) {
        return patientRepository.findById(id);
    }
    public List<User> getAllPatients() {
        return patientRepository.findAll();
    }
    public String savePatient(User patient,
                              Long categoryId,
                              Long medicoBaseId,
                              Long medicoPneumoId,
                              Long fisioterapistaId,
                              Long medicoPalId,
                              Double minHr, Double maxHr, Double minSaturation) {

        String uuid = UUID.randomUUID().toString();
        patient.setUuid(uuid);

        DiseaseCategory category = diseaseCategoryRepository.findById(categoryId).orElseThrow();
        patient.setDiseaseCategory(category);

        Set<HealthcareProfessional> professionals = new HashSet<>();
        professionals.add(healthcareProfessionalRepository.findById(medicoBaseId).orElseThrow());

        if (medicoPneumoId != null)
            professionals.add(healthcareProfessionalRepository.findById(medicoPneumoId).orElseThrow());

        if (fisioterapistaId != null)
            professionals.add(healthcareProfessionalRepository.findById(fisioterapistaId).orElseThrow());

        if (medicoPalId != null)
            professionals.add(healthcareProfessionalRepository.findById(medicoPalId).orElseThrow());

        professionals.addAll(healthcareProfessionalRepository.findByRole(Role.COORD));
        professionals.addAll(healthcareProfessionalRepository.findByRole(Role.INF));

        patient.setHealthcareProfessionals(professionals);
        patientRepository.save(patient);

        // Salva margini
        alertMarginService.saveOrUpdateMargin(patient, JsonKeys.HEART_RATE.getKey(), minHr, maxHr);
        alertMarginService.saveOrUpdateMargin(patient, JsonKeys.SPO2.getKey(), minSaturation, null);

        return patient.getAuthorizationLink();
    }

    public void updatePatient(UpdatePatientDTO dto) {
        User patient = patientRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

        patient.setName(dto.getName());
        patient.setSurname(dto.getSurname());
        patient.setBirthDate(dto.getBirthDate());
        patient.setSex(dto.getSex());
        patient.setDiagnosisDesc(dto.getDiagnosisDesc());

        DiseaseCategory category = diseaseCategoryRepository.findById(dto.getDiseaseCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        patient.setDiseaseCategory(category);

        Set<HealthcareProfessional> professionals = new HashSet<>();

        professionals.add(healthcareProfessionalRepository.findById(dto.getMedicoBaseId()).orElseThrow());
        professionals.addAll(healthcareProfessionalRepository.findByRole(Role.COORD));
        professionals.addAll(healthcareProfessionalRepository.findByRole(Role.INF));

        if (dto.getMedicoPneumoId() != null)
            professionals.add(healthcareProfessionalRepository.findById(dto.getMedicoPneumoId()).orElseThrow());

        if (dto.getFisioterapistaId() != null)
            professionals.add(healthcareProfessionalRepository.findById(dto.getFisioterapistaId()).orElseThrow());

        if (dto.getMedicoPalId() != null)
            professionals.add(healthcareProfessionalRepository.findById(dto.getMedicoPalId()).orElseThrow());

        patient.setHealthcareProfessionals(professionals);

        alertMarginService.saveOrUpdateMargin(patient, JsonKeys.HEART_RATE.getKey(), dto.getMinHr(), dto.getMaxHr());
        alertMarginService.saveOrUpdateMargin(patient, JsonKeys.SPO2.getKey(), dto.getMinSaturation(), null);

        patientRepository.save(patient);
    }

    @Transactional
    public void deletePatientById(Long id) {
        User patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

        //elimina tutti gli interventi associati al paziente
        List<MedicalIntervention> interventions = medicalInterventionRepository.findByPatient(patient);
        medicalInterventionRepository.deleteAll(interventions);

        //elimina tutte le misurazioni
        measurementRepository.deleteByPatient(patient);

        //elimina i margini
        alertMarginService.deleteByPatient(patient);

        //infine elimina il paziente
        patientRepository.delete(patient);
    }

    public List<User> findByDiseaseCategoryId(Long categoryId) {
        return patientRepository.findByDiseaseCategoryId(categoryId);
    }

    @Transactional
    public void assignProfessionalToAllPatients(HealthcareProfessional professional) {

        List<User> allPatients = patientRepository.findAll();

        for (User patient : allPatients) {
            patient.addHealthcareProfessional(professional);
        }

        patientRepository.saveAll(allPatients);
    }

}

