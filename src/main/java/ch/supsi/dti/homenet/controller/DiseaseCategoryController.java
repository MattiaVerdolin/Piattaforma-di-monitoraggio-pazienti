package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.model.DiseaseCategory;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.service.DiseaseCategoryService;
import ch.supsi.dti.homenet.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class DiseaseCategoryController {
    private final DiseaseCategoryService diseaseCategoryService;
    private final PatientService patientService;

    public DiseaseCategoryController(DiseaseCategoryService diseaseCategoryService, PatientService patientService) {
        this.diseaseCategoryService = diseaseCategoryService;
        this.patientService = patientService;
    }

    @GetMapping("/disease-categories")
    public String showDiseaseCategories(Model model) {
        model.addAttribute("categories", diseaseCategoryService.findAll());
        model.addAttribute("diseaseCategory", new DiseaseCategory());
        model.addAttribute("formAction", "/disease-categories/save");
        model.addAttribute("isEdit", false);
        return "add-disease-category";
    }

    @PostMapping("/disease-categories/save")
    public String saveCategory(@ModelAttribute DiseaseCategory diseaseCategory, RedirectAttributes redirectAttributes) {

        Optional<DiseaseCategory> existingOpt = diseaseCategoryService
                .findByName(diseaseCategory.getName());

        if (existingOpt.isPresent()) {
            DiseaseCategory existing = existingOpt.get();

            // Se è una modifica e il nome coincide con un'altra categoria esistente con ID diverso
            if (diseaseCategory.getId() == null || !diseaseCategory.getId().equals(existing.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Esiste già una categoria con lo stesso nome.");
                return "redirect:/disease-categories";
            }
        }

        diseaseCategoryService.save(diseaseCategory);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria salvata con successo.");
        return "redirect:/disease-categories";
    }


    @GetMapping("/disease-categories/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        DiseaseCategory category = diseaseCategoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        model.addAttribute("diseaseCategory", category);
        model.addAttribute("formAction", "/disease-categories/save");
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", diseaseCategoryService.findAll());
        return "add-disease-category";
    }

    @GetMapping("/disease-categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        List<User> patientsWithCategory = patientService.findByDiseaseCategoryId(id);

        if (!patientsWithCategory.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Impossibile eliminare la categoria: ci sono pazienti associati.");
        } else {
            diseaseCategoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria eliminata con successo.");
        }return "redirect:/disease-categories";
    }

}
