package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.DiseaseCategory;
import ch.supsi.dti.homenet.repository.DiseaseCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DiseaseCategoryServiceTest {

    private DiseaseCategoryRepository repository;
    private DiseaseCategoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(DiseaseCategoryRepository.class);
        service = new DiseaseCategoryService(repository);
    }

    @Test
    void shouldReturnAllCategories() {
        DiseaseCategory influenza = new DiseaseCategory();
        influenza.setName("Influenza");

        DiseaseCategory cancro = new DiseaseCategory();
        cancro.setName("Cancro");

        when(repository.findAll()).thenReturn(Arrays.asList(influenza, cancro));

        List<DiseaseCategory> result = service.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Influenza");
        assertThat(result.get(1).getName()).isEqualTo("Cancro");

        verify(repository).findAll();
    }

    @Test
    void shouldSaveCategory() {
        DiseaseCategory category = new DiseaseCategory();
        category.setName("Cancro");

        service.save(category);

        verify(repository).save(category);
    }

    @Test
    void shouldDeleteById() {
        Long id = 10L;

        service.deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    void shouldFindById() {
        DiseaseCategory category = new DiseaseCategory();
        category.setName("Influenza");

        when(repository.findById(1L)).thenReturn(Optional.of(category));

        Optional<DiseaseCategory> result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Influenza");

        verify(repository).findById(1L);
    }

    @Test
    void shouldFindByName() {
        DiseaseCategory category = new DiseaseCategory();
        category.setName("Cancro");

        when(repository.findByName("Cancro")).thenReturn(Optional.of(category));

        Optional<DiseaseCategory> result = service.findByName("Cancro");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Cancro");

        verify(repository).findByName("Cancro");
    }
}
