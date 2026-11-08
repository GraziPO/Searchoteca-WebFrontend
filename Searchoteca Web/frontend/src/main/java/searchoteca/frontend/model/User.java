package searchoteca.frontend.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Envio: espelha searchoteca.model.UserModel (role_code, password).
 * Leitura: o backend devolve UserResponse, com os nomes "userid" e "roleCode";
 * o @JsonAlias aceita os dois formatos. A senha nunca vem do backend.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonAlias("userid")
    private Long id;
    private String customId;
    private String username;
    private String completeName;
    private String email;
    private String password;
    @JsonAlias("roleCode")
    private String role_code;
    private Boolean status;

    public User() {}
}
