package ch.supsi.dti.homenet.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "healthcare_professional")
@Getter @Setter
public class HealthcareProfessional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
    @Column
    private String surname;
    @Column
    private String username;
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToMany(mappedBy = "healthcareProfessionals")
    private Set<User> patients;

}
