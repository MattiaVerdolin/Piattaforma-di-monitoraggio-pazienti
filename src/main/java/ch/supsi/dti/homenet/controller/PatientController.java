package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.AlertMargin;
import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.UpdatePatientDTO;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.service.AlertMarginService;
import ch.supsi.dti.homenet.service.DiseaseCategoryService;
import ch.supsi.dti.homenet.service.HealthcareProfessionalService;
import ch.supsi.dti.homenet.service.PatientService;
import com.google.api.client.json.Json;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PatientController {

    private final PatientService patientService;
    private final HealthcareProfessionalService healthcareProfessionalService;
    private final AlertMarginService alertMarginService;
    private final DiseaseCategoryService diseaseCategoryService;

    public PatientController(PatientService patientService, HealthcareProfessionalService healthcareProfessionalService, AlertMarginService alertMarginService, DiseaseCategoryService diseaseCategoryService) {
        this.patientService = patientService;
        this.healthcareProfessionalService = healthcareProfessionalService;
        this.alertMarginService = alertMarginService;
        this.diseaseCategoryService = diseaseCategoryService;
    }

    @GetMapping("/edit-patient")
    public String showPatientForm(
            @RequestParam(required = false) Long id,
            Model model
    ) {
        boolean isEdit = (id != null);
        User patient = isEdit
                ? patientService.findPatientById(id).orElseThrow(() -> new RuntimeException("Paziente non trovato"))
                : new User();

        if (isEdit) {
            model.addAttribute("medicoBaseId", getProfessionalIdByRole(patient, "MED_FAM"));
            model.addAttribute("medicoPneumoId", getProfessionalIdByRole(patient, "MED_PNEUM"));
            model.addAttribute("fisioterapistaId", getProfessionalIdByRole(patient, "FISIO"));
            model.addAttribute("medicoPalId", getProfessionalIdByRole(patient, "MED_PAL"));

            // Recupera soglie
            Map<String, AlertMargin> margins = alertMarginService.getMarginsByPatient(patient);
            model.addAttribute("minHr", margins.getOrDefault(JsonKeys.HEART_RATE.getKey(), new AlertMargin()).getMin());
            model.addAttribute("maxHr", margins.getOrDefault(JsonKeys.HEART_RATE.getKey(), new AlertMargin()).getMax());
            model.addAttribute("minSaturation", margins.getOrDefault(JsonKeys.SPO2.getKey(), new AlertMargin()).getMin());

        }

        model.addAttribute("patient", patient);
        model.addAttribute("categories", diseaseCategoryService.getAllCategories());
        model.addAttribute("professionals", healthcareProfessionalService.getAllProfessionals());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/patient/update" : "/add-patient");

        return "patient-form";
    }

    private Long getProfessionalIdByRole(User patient, String roleName) {
        return patient.getHealthcareProfessionals().stream()
                .filter(hp -> hp.getRole().name().equals(roleName))
                .map(HealthcareProfessional::getId)
                .findFirst()
                .orElse(null);
    }


    @PostMapping("/add-patient")
    public String savePatient(
            @ModelAttribute User patient,
            @RequestParam("diseaseCategoryId") Long categoryId,
            @RequestParam("medicoBaseId") Long medicoBaseId,
            @RequestParam(value = "medicoPneumoId", required = false) Long medicoPneumoId,
            @RequestParam(value = "fisioterapistaId", required = false) Long fisioterapistaId,
            @RequestParam(value = "medicoPalId", required = false) Long medicoPalId,
            @RequestParam(required = false) Double minHr,
            @RequestParam(required = false) Double maxHr,
            @RequestParam(required = false) Double minSaturation,
            RedirectAttributes redirectAttributes) {

        String activationLink = patientService.savePatient(
                patient, categoryId, medicoBaseId, medicoPneumoId, fisioterapistaId, medicoPalId,
                minHr, maxHr, minSaturation
        );

        redirectAttributes.addFlashAttribute("activationLink", activationLink);
        redirectAttributes.addFlashAttribute("successMessage",
                "Il paziente <strong>" + patient.getName() + " " + patient.getSurname() + "</strong> è stato aggiunto con successo.");

        return "redirect:/dashboard";
    }


    @GetMapping("/patient/delete/{id}")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        patientService.deletePatientById(id);

        redirectAttributes.addFlashAttribute("successMessage", "Paziente rimosso con successo.");
        return "redirect:/dashboard";
    }


    @PostMapping("/patient/update")
    public String updatePatient(@ModelAttribute UpdatePatientDTO dto, RedirectAttributes redirectAttributes) {
        patientService.updatePatient(dto);

        redirectAttributes.addFlashAttribute("successMessage", "Paziente aggiornato con successo.");
        return "redirect:/dashboard";
    }


}
