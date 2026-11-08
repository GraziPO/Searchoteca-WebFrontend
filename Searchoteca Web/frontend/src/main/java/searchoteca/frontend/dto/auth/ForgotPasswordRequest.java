package searchoteca.frontend.dto.auth;

/** Corpo de POST /api/auth/senha/esqueci. "login" aceita username ou e-mail. */
public record ForgotPasswordRequest(String login) {}
