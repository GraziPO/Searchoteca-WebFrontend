package searchoteca.frontend.dto.auth;

/** Corpo de POST /api/auth/senha/primeiro-acesso. */
public record FirstAccessRequest(String passwordChangeToken, String newPassword) {}
