package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.*;
import ch.supsi.dti.homenet.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class MainController {

    private final HealthcareProfessionalService healthcareProfessionalService;
    private final PatientService patientService;
    private final MedicalInterventionService medicalInterventionService;
    private final MeasurementService measurementService;
    private final AlertMarginService alertMarginService;
    public MainController(HealthcareProfessionalService healthcareProfessionalService, PatientService patientService, MedicalInterventionService medicalInterventionService, MeasurementService measurementService, AlertMarginService alertMarginService) {
        this.healthcareProfessionalService = healthcareProfessionalService;
        this.patientService = patientService;
        this.medicalInterventionService = medicalInterventionService;
        this.measurementService = measurementService;
        this.alertMarginService = alertMarginService;
    }

    @GetMapping("")
    public String home() {
        return "login"; // Loads login.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Loads login.html
    }

    @GetMapping("/error")
    public String errorPage() {
        return "error"; // Loads error.html
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        HealthcareProfessional professional = healthcareProfessionalService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Healthcare professional not found"));

        // Verifica se l'utente ha il ruolo COORD
        boolean isCoord = professional.getRole() == Role.COORD;

        Set<User> patients;
        if (isCoord) {
            // Mostra tutti i pazienti se l'utente è COORD
            patients = new HashSet<>(patientService.getAllPatients());
        } else {
            // Mostra solo i pazienti associati al professionista
            patients = (professional.getPatients() != null) ? professional.getPatients() : Collections.emptySet();
        }

        List<HealthcareProfessional> healthcarePersonnel = healthcareProfessionalService.getAll();

        model.addAttribute("currentUser", professional);
        model.addAttribute("patients", patients);
        model.addAttribute("healthcarePersonnel", healthcarePersonnel);

        return "dashboard";
    }

    @GetMapping("/api/patient/biometric-data")
    @ResponseBody
    public Map<String, Object> getBiometricData(@RequestParam Long patientId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Date selectedDate = java.sql.Date.valueOf(date);
        Map<String, Object> response = new HashMap<>();

        try {
            User patient = patientService.findPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            // Recupera i dati per le card
            Map<String, String> heartRateValues = measurementService.getHeartRateData(patient, selectedDate);
            Map<String, String> spo2Values = measurementService.getSpo2Data(patient, selectedDate);
            Map<String, String> stressValues = measurementService.getStressData(patient, selectedDate);
            Map<String, String> stepsValues = measurementService.getStepsData(patient, selectedDate); // <<< ADD THIS LINE

            // Recupera il timeline per i grafici
            List<Map<String, Object>> heartRateTimeline = measurementService.getHeartRateTimeline(patient, selectedDate);
            List<Map<String, Object>> spo2Timeline = measurementService.getSpo2Timeline(patient, selectedDate);

            response.put("heartRate", Map.of(
                    "max", heartRateValues.get("max"),
                    "avg", heartRateValues.get("avg"),
                    "min", heartRateValues.get("min"),
                    "timeline", heartRateTimeline
            ));

            response.put("spo2", Map.of(
                    "max", spo2Values.get("max"),
                    "avg", spo2Values.get("avg"),
                    "min", spo2Values.get("min"),
                    "timeline", spo2Timeline
            ));

            response.put("stress", stressValues);

            response.put("steps", stepsValues); // <<< ADD THIS LINE

        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }


    @GetMapping("/patient/{id}")
    public String viewPatient(@PathVariable Long id, Model model) throws JsonProcessingException {
        Optional<User> patientOpt = patientService.findPatientById(id);

        if (patientOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        User patient = patientOpt.get();
        List<MedicalIntervention> interventions = medicalInterventionService.findByPatient(patient);

        List<Map<String, Object>> events = new ArrayList<>();
        for (MedicalIntervention intervention : interventions) {
            Map<String, Object> event = new HashMap<>();
            event.put("title", intervention.getTitle());

            //formattato per passare anche l'ora a full calendar
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            event.put("start", intervention.getDate().format(formatter));
            event.put("allDay", false);

            event.put("url", "/intervention/" + intervention.getId());

            // Role-based badge color
            String className = switch (intervention.getHealthcareProfessional().getRole()) {
                case COORD -> "badge bg-secondary";
                case INF -> "badge bg-info text-dark";
                case FISIO -> "badge bg-warning text-dark";
                case MED_PAL -> "badge bg-danger";
                case MED_PNEUM -> "badge bg-success";
                case MED_FAM -> "badge bg-primary";
            };

            event.put("className", className);

            events.add(event);
        }
        ObjectMapper mapper = new ObjectMapper();
        String eventsJson = mapper.writeValueAsString(events);

        Map<String, AlertMargin> margins = alertMarginService.getMarginsByPatient(patient);

// Default values if margin not present
        double minHr = Optional.ofNullable(margins.get(JsonKeys.HEART_RATE.getKey()))
                .map(AlertMargin::getMin)
                .orElse(0.0);  // oppure usa null, oppure un valore di fallback specifico

        double maxHr = Optional.ofNullable(margins.get(JsonKeys.HEART_RATE.getKey()))
                .map(AlertMargin::getMax)
                .orElse(0.0);

        double minSpo2 = Optional.ofNullable(margins.get(JsonKeys.SPO2.getKey()))
                .map(AlertMargin::getMin)
                .orElse(0.0);

// Crea JSON dei margini
        Map<String, Object> marginsJson = new HashMap<>();
        marginsJson.put("minHr", minHr);
        marginsJson.put("maxHr", maxHr);
        marginsJson.put("minSpo2", minSpo2);

        String marginsJsonString = mapper.writeValueAsString(marginsJson);

// Passa al model per Thymeleaf o frontend
        model.addAttribute("margins", marginsJsonString);



        model.addAttribute("patient", patient);
        model.addAttribute("eventsJson", eventsJson);

        return "patient-details";
    }

    @GetMapping("/intervention/{id}")
    public String viewIntervention(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<MedicalIntervention> interventionOpt = medicalInterventionService.findById(id);

        if (interventionOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        MedicalIntervention intervention = interventionOpt.get();

        // Ottieni il nome utente dell'utente autenticato
        String loggedUsername = authentication.getName();

        // Verifica se l'utente è autorizzato
        boolean isAuthorized = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COORD")) ||
                (intervention.getHealthcareProfessional() != null &&
                        loggedUsername.equals(intervention.getHealthcareProfessional().getUsername()));

        // Aggiungi i dati al modello
        model.addAttribute("intervention", intervention);
        model.addAttribute("patient", intervention.getPatient());
        model.addAttribute("isAuthorized", isAuthorized);

        return "intervention-details";
    }
}
