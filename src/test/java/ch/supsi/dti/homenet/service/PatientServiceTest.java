package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.*;
import ch.supsi.dti.homenet.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientServiceTest {
    private DiseaseCategoryRepository categoryRepository;
    private HealthcareProfessionalRepository professionalRepository;
    private UserRepository patientRepository;
    private AlertMarginService alertMarginService;
    private MedicalInterventionRepository medicalInterventionRepository;
    private MeasurementRepository measurementRepository;

    private PatientService service;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(DiseaseCategoryRepository.class);
        professionalRepository = mock(HealthcareProfessionalRepository.class);
        patientRepository = mock(UserRepository.class);
        medicalInterventionRepository = mock(MedicalInterventionRepository.class);
        measurementRepository = mock(MeasurementRepository.class);
        alertMarginService = mock(AlertMarginService.class);

        service = new PatientService(categoryRepository, professionalRepository, patientRepository, medicalInterventionRepository, measurementRepository, alertMarginService);
    }

    @Test
    void testFindPatientById_found() {
        User patient = new User();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Optional<User> result = service.findPatientById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testDeletePatientById_success() {
        User patient = new User();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        service.deletePatientById(1L);

        verify(alertMarginService).deleteByPatient(patient);
        verify(patientRepository).delete(patient);
    }

    @Test
    void testAssignProfessionalToAllPatients_addsCorrectly() {
        HealthcareProfessional professional = new HealthcareProfessional();
        User u1 = new User();
        u1.setHealthcareProfessionals(new HashSet<>());

        User u2 = new User();
        u2.setHealthcareProfessionals(new HashSet<>());

        when(patientRepository.findAll()).thenReturn(List.of(u1, u2));

        service.assignProfessionalToAllPatients(professional);

        assertTrue(u1.getHealthcareProfessionals().contains(professional));
        assertTrue(u2.getHealthcareProfessionals().contains(professional));
        verify(patientRepository).saveAll(List.of(u1, u2));
    }

    @Test
    void testFindByDiseaseCategoryId_success() {
        User p1 = new User();
        when(patientRepository.findByDiseaseCategoryId(99L)).thenReturn(List.of(p1));

        List<User> result = service.findByDiseaseCategoryId(99L);
        assertEquals(1, result.size());
    }

    @Test
    void testSavePatient_generatesUUIDAndSaves() {
        User patient = new User();
        DiseaseCategory cat = new DiseaseCategory();
        HealthcareProfessional base = new HealthcareProfessional();
        HealthcareProfessional coord = new HealthcareProfessional();
        HealthcareProfessional inf = new HealthcareProfessional();

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(base));
        when(professionalRepository.findByRole(Role.COORD)).thenReturn(List.of(coord));
        when(professionalRepository.findByRole(Role.INF)).thenReturn(List.of(inf));

        String url = service.savePatient(patient, 10L, 1L, null, null, null, 60.0, 100.0, 95.0);

        assertNotNull(patient.getUuid());
        assertTrue(url.contains(patient.getUuid()));
        verify(patientRepository).save(patient);
        verify(alertMarginService).saveOrUpdateMargin(patient, JsonKeys.HEART_RATE.getKey(), 60.0, 100.0);
        verify(alertMarginService).saveOrUpdateMargin(patient, JsonKeys.SPO2.getKey(), 95.0, null);
    }

    @Test
    void testUpdatePatient_updatesFieldsCorrectly() {
        User patient = new User();
        patient.setHealthcareProfessionals(new HashSet<>());

        DiseaseCategory cat = new DiseaseCategory();
        cat.setId(3L);

        HealthcareProfessional base = new HealthcareProfessional();
        HealthcareProfessional coord = new HealthcareProfessional();
        HealthcareProfessional inf = new HealthcareProfessional();

        UpdatePatientDTO dto = new UpdatePatientDTO();
        dto.setId(1L);
        dto.setName("Mario");
        dto.setSurname("Rossi");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setSex("M");
        dto.setDiagnosisDesc("Diagnosi");
        dto.setDiseaseCategoryId(3L);
        dto.setMedicoBaseId(1L);
        dto.setMinHr(50.0);
        dto.setMaxHr(90.0);
        dto.setMinSaturation(94.0);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(cat));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(base));
        when(professionalRepository.findByRole(Role.COORD)).thenReturn(List.of(coord));
        when(professionalRepository.findByRole(Role.INF)).thenReturn(List.of(inf));

        service.updatePatient(dto);

        assertEquals("Mario", patient.getName());
        assertEquals("Rossi", patient.getSurname());
        assertEquals("Diagnosi", patient.getDiagnosisDesc());
        assertEquals(cat, patient.getDiseaseCategory());

        verify(alertMarginService).saveOrUpdateMargin(patient, JsonKeys.HEART_RATE.getKey(), 50.0, 90.0);
        verify(alertMarginService).saveOrUpdateMargin(patient, JsonKeys.SPO2.getKey(), 94.0, null);
        verify(patientRepository).save(patient);
    }
}
