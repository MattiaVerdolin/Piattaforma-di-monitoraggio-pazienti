package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.InterventionDTO;
import ch.supsi.dti.homenet.model.MedicalIntervention;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MedicalInterventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MedicalInterventionServiceTest {
    private MedicalInterventionRepository repository;
    private MedicalInterventionService service;

    @BeforeEach
    void setUp() {
        repository = mock(MedicalInterventionRepository.class);
        service = new MedicalInterventionService(repository);
    }

    @Test
    void testFindByPatient_success() {
        User patient = new User();
        MedicalIntervention i1 = new MedicalIntervention();
        MedicalIntervention i2 = new MedicalIntervention();
        when(repository.findByPatient(patient)).thenReturn(Arrays.asList(i1, i2));

        List<MedicalIntervention> result = service.findByPatient(patient);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByPatient_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.findByPatient(null));
    }

    @Test
    void testFindById_success() {
        MedicalIntervention intervention = new MedicalIntervention();
        when(repository.findById(1L)).thenReturn(Optional.of(intervention));

        Optional<MedicalIntervention> result = service.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindById_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void testSave_callsRepositorySave() {
        MedicalIntervention intervention = new MedicalIntervention();
        service.save(intervention);

        verify(repository).save(intervention);
    }

    @Test
    void testDeleteById_callsRepositoryDelete() {
        service.deleteById(5L);
        verify(repository).deleteById(5L);
    }

    @Test
    void testUpdateIntervention_success() {
        MedicalIntervention existing = new MedicalIntervention();
        existing.setId(100L);
        when(repository.findById(100L)).thenReturn(Optional.of(existing));

        InterventionDTO dto = new InterventionDTO();
        dto.setId(100L);
        dto.setTitle("New Title");
        dto.setDescription("Updated Description");

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalIntervention updated = service.updateIntervention(dto);

        assertEquals("New Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
    }

    @Test
    void testUpdateIntervention_notFound_throwsException() {
        InterventionDTO dto = new InterventionDTO();
        dto.setId(200L);

        when(repository.findById(200L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.updateIntervention(dto));
        assertEquals("Intervento non trovato", exception.getMessage());
    }
}
