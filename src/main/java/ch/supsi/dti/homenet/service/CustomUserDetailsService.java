package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.repository.HealthcareProfessionalRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final HealthcareProfessionalRepository repository;

    public CustomUserDetailsService(HealthcareProfessionalRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HealthcareProfessional user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Ensure the password is hashed!
                .roles(user.getRole().name()) // Use the role defined in HealthcareProfessional
                .build();
    }
}
