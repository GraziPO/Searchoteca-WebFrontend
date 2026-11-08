package searchoteca.frontend.dto.auth;

/** Corpo de POST /api/usuarios/me/senha (troca voluntária, usuário logado). */
public record ChangePasswordRequest(String currentPassword, String newPassword) {}
