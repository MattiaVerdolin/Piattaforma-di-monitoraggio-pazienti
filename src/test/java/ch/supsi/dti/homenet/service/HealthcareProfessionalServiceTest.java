package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.HealthcareProfessional;
import ch.supsi.dti.homenet.model.Role;
import ch.supsi.dti.homenet.model.UpdateHpDTO;
import ch.supsi.dti.homenet.repository.HealthcareProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HealthcareProfessionalServiceTest {

    @Mock
    private HealthcareProfessionalRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HealthcareProfessionalService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUsername() {
        HealthcareProfessional hp = new HealthcareProfessional();
        hp.setUsername("drsmith");

        when(repository.findByUsername("drsmith")).thenReturn(Optional.of(hp));

        Optional<HealthcareProfessional> result = service.findByUsername("drsmith");

        assertThat(result).isPresent().contains(hp);
        verify(repository).findByUsername("drsmith");
    }

    @Test
    void testGetCurrentHealthcareProfessional() {
        String username = "drjohn";
        HealthcareProfessional hp = new HealthcareProfessional();
        hp.setUsername(username);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(repository.findByUsername(username)).thenReturn(Optional.of(hp));

        HealthcareProfessional result = service.getCurrentHealthcareProfessional();

        assertThat(result).isEqualTo(hp);
    }


    @Test
    void testSaveShouldEncodePasswordAndSave() {
        HealthcareProfessional hp = new HealthcareProfessional();
        hp.setPassword("plaintext");

        when(passwordEncoder.encode("plaintext")).thenReturn("hashed");

        service.save(hp);

        assertThat(hp.getPassword()).isEqualTo("hashed");
        verify(repository).save(hp);
    }

    @Test
    void testUpdateShouldModifyAndSave() {
        UpdateHpDTO dto = new UpdateHpDTO();
        dto.setId(1L);
        dto.setName("Anna");
        dto.setSurname("Verdi");
        dto.setUsername("annaverdi");
        dto.setRole(Role.COORD);
        dto.setPassword("secret");

        HealthcareProfessional hp = new HealthcareProfessional();
        hp.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(hp));
        when(passwordEncoder.encode("secret")).thenReturn("hashedSecret");

        service.update(dto);

        assertThat(hp.getName()).isEqualTo("Anna");
        assertThat(hp.getPassword()).isEqualTo("hashedSecret");
        verify(repository).save(hp);
    }

    @Test
    void testDeleteByIdShouldRemoveReferencesAndDelete() {
        HealthcareProfessional hp = new HealthcareProfessional();
        hp.setId(2L);
        hp.setPatients(new HashSet<>());

        when(repository.findById(2L)).thenReturn(Optional.of(hp));

        service.deleteById(2L);

        verify(repository).deleteById(2L);
    }

    @Test
    void testGetAllRoles() {
        List<Role> roles = service.getAllRoles();
        assertThat(roles).contains(Role.COORD, Role.INF);
    }
}
