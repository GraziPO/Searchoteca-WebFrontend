package searchoteca.frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.dto.AuthResult;
import searchoteca.frontend.dto.LegalDocument;
import searchoteca.frontend.dto.LoginRequest;
import searchoteca.frontend.dto.MfaResendRequest;
import searchoteca.frontend.dto.MfaVerifyRequest;
import searchoteca.frontend.dto.TermsAcceptRequest;
import searchoteca.frontend.dto.auth.FirstAccessRequest;
import searchoteca.frontend.dto.auth.ForgotPasswordRequest;
import searchoteca.frontend.dto.auth.ResetPasswordRequest;

@Service
public class AuthService {

    private final RestClient restClient;

    public AuthService(RestClient backendRestClient) {
        this.restClient = backendRestClient;
    }

    /** Etapa 1: usuario + senha. Pode devolver termos pendentes, desafio de MFA, troca de senha ou o token. */
    public AuthResult login(String username, String password) {
        return restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Etapa 2 (se pendente): registra o aceite; devolve desafio de MFA, troca de senha ou o token. */
    public AuthResult acceptTerms(String termsToken, String termsVersion, String privacyVersion) {
        return restClient.post()
                .uri("/api/auth/termos/accept")   // o backend mudou de /terms/accept para /termos/accept
                .body(new TermsAcceptRequest(termsToken, termsVersion, privacyVersion))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Etapa 3: confere o codigo enviado por e-mail. Devolve o JWT ou o desafio de troca de senha. */
    public AuthResult verifyMfa(String mfaToken, String code) {
        return restClient.post()
                .uri("/api/auth/mfa/verify")
                .body(new MfaVerifyRequest(mfaToken, code))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Reenvia o codigo (o anterior deixa de valer). */
    public AuthResult resendMfa(String mfaToken) {
        return restClient.post()
                .uri("/api/auth/mfa/resend")
                .body(new MfaResendRequest(mfaToken))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Etapa 4 (primeiro acesso): troca a senha inicial e devolve o JWT. */
    public AuthResult completeFirstAccess(String passwordChangeToken, String newPassword) {
        return restClient.post()
                .uri("/api/auth/senha/primeiro-acesso")
                .body(new FirstAccessRequest(passwordChangeToken, newPassword))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Esqueci a senha: o backend responde igual exista o usuário ou não. */
    public void requestPasswordReset(String login) {
        restClient.post()
                .uri("/api/auth/senha/esqueci")
                .body(new ForgotPasswordRequest(login))
                .retrieve()
                .toBodilessEntity();
    }

    public void resetPassword(String login, String code, String newPassword) {
        restClient.post()
                .uri("/api/auth/senha/redefinir")
                .body(new ResetPasswordRequest(login, code, newPassword))
                .retrieve()
                .toBodilessEntity();
    }

    public LegalDocument getTerms() {
        return restClient.get().uri("/api/legal/termos").retrieve().body(LegalDocument.class);
    }

    public LegalDocument getPrivacyPolicy() {
        return restClient.get().uri("/api/legal/privacidade").retrieve().body(LegalDocument.class);
    }
}
