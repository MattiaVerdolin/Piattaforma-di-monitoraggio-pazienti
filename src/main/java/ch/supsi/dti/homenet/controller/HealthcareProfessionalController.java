package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.Role;
import ch.supsi.dti.homenet.model.UpdateHpDTO;
import ch.supsi.dti.homenet.service.HealthcareProfessionalService;
import ch.supsi.dti.homenet.service.PatientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HealthcareProfessionalController {

    private final HealthcareProfessionalService healthcareProfessionalService;
    private final PatientService patientService;

    public HealthcareProfessionalController(HealthcareProfessionalService healthcareProfessionalService, PatientService patientService) {
        this.healthcareProfessionalService = healthcareProfessionalService;
        this.patientService = patientService;
    }

    @GetMapping("/healthcare-professional/delete/{id}")
    public String deleteProfessional(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            healthcareProfessionalService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Figura sanitaria rimossa con successo.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/healthcare-professional/{id}")
    public String showProfessionalDetails(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          Model model) {
        HealthcareProfessional professional = healthcareProfessionalService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professionista non trovato con ID: " + id));

        model.addAttribute("professional", professional);
        model.addAttribute("loggedUser", healthcareProfessionalService.findByUsername(userDetails.getUsername()).orElse(null));
        return "hp-details";
    }

    @GetMapping("/edit-professional")
    public String showAddProfessionalForm(
            @RequestParam(required = false) Long id,
            Model model) {

        boolean isEdit = (id != null);
        HealthcareProfessional professional = isEdit ? healthcareProfessionalService.findById(id).orElseThrow(() -> new RuntimeException("Figura sanitaria non trovato"))
                : new HealthcareProfessional();

        model.addAttribute("professional", professional);
        model.addAttribute("roles", healthcareProfessionalService.getAllRoles());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/professional/update" : "/add-professional");
        return "hp-form";
    }

    @PostMapping("/professional/update")
    public String updateProfessional(@ModelAttribute UpdateHpDTO dto,
                                     RedirectAttributes redirectAttributes) {

        healthcareProfessionalService.update(dto);

        redirectAttributes.addFlashAttribute("successMessage", "Figura sanitaria aggiornata con successo.");
        return "redirect:/dashboard";
    }


    @PostMapping("/add-professional")
    public String saveProfessional(
            @ModelAttribute HealthcareProfessional professional,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (!professional.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le password non coincidono.");
            redirectAttributes.addFlashAttribute("professional", professional);
            redirectAttributes.addFlashAttribute("isEdit", false);
            return "redirect:/edit-professional";
        }

        healthcareProfessionalService.save(professional);

        //se è un coordinatore o un infermiere, possono vedere tutti i pazienti
        if(professional.getRole() == Role.INF || professional.getRole() == Role.COORD) {
            patientService.assignProfessionalToAllPatients(professional);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "La figura sanitaria <strong>" + professional.getName() + " " + professional.getSurname() + "</strong> è stata aggiunta con successo."
        );

        return "redirect:/dashboard";
    }

}
