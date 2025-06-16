package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.Role;
import ch.supsi.dti.homenet.repository.HealthcareProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomUserDetailServiceTest {

    private HealthcareProfessionalRepository repository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        repository = mock(HealthcareProfessionalRepository.class);
        service = new CustomUserDetailsService(repository);
    }

    @Test
    void shouldLoadUserByUsername_WhenUserExists() {
        // Given
        HealthcareProfessional user = new HealthcareProfessional();
        user.setUsername("mario");
        user.setPassword("hashedPassword");
        user.setRole(Role.COORD);

        when(repository.findByUsername("mario")).thenReturn(Optional.of(user));

        // When
        UserDetails result = service.loadUserByUsername("mario");

        // Then
        assertThat(result.getUsername()).isEqualTo("mario");
        assertThat(result.getPassword()).isEqualTo("hashedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_COORD");

        verify(repository).findByUsername("mario");
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        when(repository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("notfound"));

        verify(repository).findByUsername("notfound");
    }
}
