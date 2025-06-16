package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.repository.HealthcareProfessionalRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final HealthcareProfessionalRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(HealthcareProfessionalRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Hashing passwords
    }

    public Optional<HealthcareProfessional> authenticate(String username, String password) {
        Optional<HealthcareProfessional> user = repository.findByUsername(username);
        return user.filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    public HealthcareProfessional register(HealthcareProfessional professional) {
        professional.setPassword(passwordEncoder.encode(professional.getPassword())); // Hash password
        return repository.save(professional);
    }

}
