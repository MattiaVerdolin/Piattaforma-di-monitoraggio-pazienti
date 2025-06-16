package ch.supsi.dti.homenet.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHpDTO {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private Role role;
    private String password;
}
