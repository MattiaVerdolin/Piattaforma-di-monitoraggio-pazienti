package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.service.HealthcareProfessionalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final HealthcareProfessionalService healthcareProfessionalService;

    public GlobalModelAttributes(HealthcareProfessionalService healthcareProfessionalService) {
        this.healthcareProfessionalService = healthcareProfessionalService;
    }

    // Add the currently logged-in user to the model for every page
    @ModelAttribute("loggedUser")
    public HealthcareProfessional getLoggedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        return healthcareProfessionalService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Healthcare professional not found"));
    }
}

