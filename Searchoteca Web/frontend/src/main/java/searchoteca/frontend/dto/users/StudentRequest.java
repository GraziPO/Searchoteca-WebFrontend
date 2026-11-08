package searchoteca.frontend.dto.users;

/** Corpo de POST /api/usuarios/aluno. O backend fixa o perfil "students" e exige a troca da senha no primeiro acesso. */
public record StudentRequest(String username, String completeName, String email,
                             String password, String ra, String phone) {}
