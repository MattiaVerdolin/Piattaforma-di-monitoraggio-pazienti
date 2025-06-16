package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.model.*;
import ch.supsi.dti.homenet.service.HealthcareProfessionalService;
import ch.supsi.dti.homenet.service.MedicalInterventionService;
import ch.supsi.dti.homenet.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class InterventionController {
    private final PatientService patientService;
    private final HealthcareProfessionalService healthcareProfessionalService;
    private final MedicalInterventionService medicalInterventionService;

    public InterventionController(PatientService patientService, HealthcareProfessionalService healthcareProfessionalService, MedicalInterventionService medicalInterventionService) {
        this.patientService = patientService;
        this.healthcareProfessionalService = healthcareProfessionalService;
        this.medicalInterventionService = medicalInterventionService;
    }

    @GetMapping("/edit-intervention")
    public String showInterventionForm(@RequestParam(required = false) Long id,
                                       @RequestParam(required = false) Long patientId, Model model) {

        boolean isEdit = (id != null);

        MedicalIntervention medicalIntervention = isEdit ?
                medicalInterventionService.findById(id).orElseThrow(() -> new RuntimeException("Intervento non trovato"))
                : new MedicalIntervention();

        User patient = patientService.findPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

        model.addAttribute("patient", patient);
        model.addAttribute("intervention", medicalIntervention);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/intervention/update" : "/save-intervention");

//        if (!isEdit && patientId != null) {
//            model.addAttribute("patientId", patientId);
//        } else if (isEdit && medicalIntervention.getPatient() != null) {
//            model.addAttribute("patientId", medicalIntervention.getPatient().getId());
//        }

        return "intervention-form";
    }

    @GetMapping("/intervention/delete/{id}")
    public String deleteIntervention(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        MedicalIntervention intervention = medicalInterventionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervento non trovato"));
        Long patientId = intervention.getPatient().getId();

        medicalInterventionService.deleteById(id);

        redirectAttributes.addFlashAttribute("successMessage", "Intervento eliminato correttamente.");
        return "redirect:/patient/" + patientId;
    }


    @PostMapping("/save-intervention")
    public String saveIntervention(@ModelAttribute MedicalIntervention medicalIntervention,
                                   @RequestParam Long patientId,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        User patient = patientService.findPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));
        medicalIntervention.setPatient(patient);

        HealthcareProfessional hp = healthcareProfessionalService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Figura sanitaria non trovata"));
        medicalIntervention.setHealthcareProfessional(hp);

        medicalInterventionService.save(medicalIntervention);

        redirectAttributes.addFlashAttribute("successMessage", "Intervento salvato correttamente.");
        return "redirect:/patient/" + patientId;
    }




    @PostMapping("/intervention/update")
    public String updateIntervention(@ModelAttribute InterventionDTO dto,
                                     RedirectAttributes redirectAttributes) {

        MedicalIntervention updated = medicalInterventionService.updateIntervention(dto);
        Long patientId = updated.getPatient().getId();

        redirectAttributes.addFlashAttribute("successMessage", "Intervento aggiornato correttamente.");
        return "redirect:/patient/" + patientId;
    }


}
