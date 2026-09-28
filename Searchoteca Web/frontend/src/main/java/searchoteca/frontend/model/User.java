package searchoteca.frontend.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Espelha searchoteca.model.UserModel no backend.
 */
@Getter
@Setter
public class User {
    private Long id;
    private String customId;
    private String username;
    private String completeName;
    private String email;
    private String password;
    private String role_code;
    private Boolean status;

    public User() {}
}
