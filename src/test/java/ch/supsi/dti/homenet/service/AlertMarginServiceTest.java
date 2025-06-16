package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.AlertMargin;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.AlertMarginRepository;
import ch.supsi.dti.homenet.repository.MeasurementTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlertMarginServiceTest {

    @Mock
    private AlertMarginRepository alertMarginRepository;

    @Mock
    private MeasurementTypeRepository measurementTypeRepository;

    @InjectMocks
    private AlertMarginService alertMarginService;

    private User patient;
    private MeasurementType type;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new User();
        patient.setId(1L);

        type = new MeasurementType();
        type.setId(1L);
        type.setName("HEART_RATE");
    }

    @Test
    void testSaveOrUpdateMargin_NewEntry() {
        when(measurementTypeRepository.findByName("HEART_RATE")).thenReturn(type);
        when(alertMarginRepository.findByPatientAndMeasurementType(patient, type)).thenReturn(Optional.empty());

        alertMarginService.saveOrUpdateMargin(patient, "HEART_RATE", 60.0, 100.0);

        ArgumentCaptor<AlertMargin> captor = ArgumentCaptor.forClass(AlertMargin.class);
        verify(alertMarginRepository).save(captor.capture());

        AlertMargin saved = captor.getValue();
        assertThat(saved.getMin()).isEqualTo(60.0);
        assertThat(saved.getMax()).isEqualTo(100.0);
        assertThat(saved.getPatient()).isEqualTo(patient);
        assertThat(saved.getMeasurementType()).isEqualTo(type);
    }

    @Test
    void testSaveOrUpdateMargin_ExistingEntry() {
        AlertMargin existing = new AlertMargin();
        existing.setMeasurementType(type);
        existing.setPatient(patient);

        when(measurementTypeRepository.findByName("HEART_RATE")).thenReturn(type);
        when(alertMarginRepository.findByPatientAndMeasurementType(patient, type)).thenReturn(Optional.of(existing));

        alertMarginService.saveOrUpdateMargin(patient, "HEART_RATE", 50.0, 90.0);

        assertThat(existing.getMin()).isEqualTo(50.0);
        assertThat(existing.getMax()).isEqualTo(90.0);
        verify(alertMarginRepository).save(existing);
    }

    @Test
    void testSaveOrUpdateMargin_NullMinAndMax_ShouldSkip() {
        alertMarginService.saveOrUpdateMargin(patient, "HEART_RATE", null, null);
        verifyNoInteractions(measurementTypeRepository);
        verifyNoInteractions(alertMarginRepository);
    }

    @Test
    void testGetMarginsByPatient() {
        AlertMargin margin = new AlertMargin();
        margin.setMeasurementType(type);
        margin.setPatient(patient);

        when(alertMarginRepository.findAllByPatient(patient)).thenReturn(List.of(margin));

        Map<String, AlertMargin> result = alertMarginService.getMarginsByPatient(patient);
        assertThat(result).hasSize(1);
        assertThat(result.get("HEART_RATE")).isEqualTo(margin);
    }

    @Test
    void testDeleteByPatient() {
        alertMarginService.deleteByPatient(patient);
        verify(alertMarginRepository).deleteByPatient(patient);
    }
}
