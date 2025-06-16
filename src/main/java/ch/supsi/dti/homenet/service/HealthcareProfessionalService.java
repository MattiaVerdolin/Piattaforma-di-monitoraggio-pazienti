package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.Role;
import ch.supsi.dti.homenet.model.UpdateHpDTO;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.HealthcareProfessionalRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HealthcareProfessionalService {

    private final HealthcareProfessionalRepository healthcareProfessionalRepository;
    private final PasswordEncoder passwordEncoder;

    public HealthcareProfessionalService(HealthcareProfessionalRepository healthcareProfessionalRepository, PasswordEncoder passwordEncoder) {
        this.healthcareProfessionalRepository = healthcareProfessionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<HealthcareProfessional> findByUsername(String username) {
        return healthcareProfessionalRepository.findByUsername(username);
    }

    public HealthcareProfessional getCurrentHealthcareProfessional() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Healthcare professional not found: " + username));
    }

    public List<HealthcareProfessional> getAll() {
        return healthcareProfessionalRepository.findAll();
    }

    public Set<User> getPatientsForCurrentProfessional() {
        HealthcareProfessional professional = getCurrentHealthcareProfessional();
        return professional.getPatients();
    }

    @Transactional
    public void deleteById(Long id) {
        HealthcareProfessional hp = healthcareProfessionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professionista non trovato"));

        if (hp.getPatients() != null) {
            for (User patient : hp.getPatients()) {
                patient.getHealthcareProfessionals().remove(hp);
            }
        }

        healthcareProfessionalRepository.deleteById(id);
    }


    public Optional<HealthcareProfessional> findById(Long id) {
        return healthcareProfessionalRepository.findById(id);
    }

    public void save(HealthcareProfessional hp) {
        hp.setPassword(passwordEncoder.encode(hp.getPassword()));
        healthcareProfessionalRepository.save(hp);
    }

    public void update(UpdateHpDTO dto) {
        HealthcareProfessional hp = healthcareProfessionalRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Figura sanitaria non trovata"));

        hp.setName(dto.getName());
        hp.setSurname(dto.getSurname());
        hp.setUsername(dto.getUsername());
        hp.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            hp.setPassword(encodedPassword);
        }

        healthcareProfessionalRepository.save(hp);
    }

    public List<HealthcareProfessional> getAllProfessionals() {
        return healthcareProfessionalRepository.findAll();
    }

    public List<Role> getAllRoles() {
        return Arrays.asList(Role.values());
    }


}

