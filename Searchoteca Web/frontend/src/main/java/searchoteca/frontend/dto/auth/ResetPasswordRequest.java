package searchoteca.frontend.dto.auth;

/** Corpo de POST /api/auth/senha/redefinir. */
public record ResetPasswordRequest(String login, String code, String newPassword) {}
