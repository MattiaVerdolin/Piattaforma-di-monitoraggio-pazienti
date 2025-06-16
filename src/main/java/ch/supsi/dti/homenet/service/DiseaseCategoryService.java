package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.DiseaseCategory;
import ch.supsi.dti.homenet.repository.DiseaseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiseaseCategoryService {
    private final DiseaseCategoryRepository diseaseCategoryRepository;

    public DiseaseCategoryService(DiseaseCategoryRepository diseaseCategoryRepository) {
        this.diseaseCategoryRepository = diseaseCategoryRepository;
    }

    public List<DiseaseCategory> getAllCategories() {
        return diseaseCategoryRepository.findAll();
    }

    public List<DiseaseCategory> findAll() {
        return diseaseCategoryRepository.findAll();
    }

    public void save(DiseaseCategory diseaseCategory) {
        diseaseCategoryRepository.save(diseaseCategory);
    }

    public void deleteById(Long id) {
        diseaseCategoryRepository.deleteById(id);
    }

    public Optional<DiseaseCategory> findById(Long id) {
        return diseaseCategoryRepository.findById(id);
    }

    public Optional<DiseaseCategory> findByName(String name) {
        return diseaseCategoryRepository.findByName(name);
    }

}
