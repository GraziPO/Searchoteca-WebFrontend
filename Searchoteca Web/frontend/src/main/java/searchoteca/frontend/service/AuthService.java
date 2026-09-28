package searchoteca.frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.dto.AuthResult;
import searchoteca.frontend.dto.LegalDocument;
import searchoteca.frontend.dto.LoginRequest;
import searchoteca.frontend.dto.MfaResendRequest;
import searchoteca.frontend.dto.MfaVerifyRequest;
import searchoteca.frontend.dto.TermsAcceptRequest;

@Service
public class AuthService {

    private final RestClient restClient;

    public AuthService(RestClient backendRestClient) {
        this.restClient = backendRestClient;
    }

    /** Etapa 1: usuario + senha. Pode devolver termos pendentes, desafio de MFA ou o token direto. */
    public AuthResult login(String username, String password) {
        return restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Etapa 2 (se pendente): registra o aceite; devolve desafio de MFA ou o token. */
    public AuthResult acceptTerms(String termsToken, String termsVersion, String privacyVersion) {
        return restClient.post()
                .uri("/api/auth/terms/accept")
                .body(new TermsAcceptRequest(termsToken, termsVersion, privacyVersion))
                .retrieve()
                .body(AuthResult.class);
    }

    /** Etapa 3: confere o codigo enviado por e-mail e devolve o JWT. */
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

    public LegalDocument getTerms() {
        return restClient.get().uri("/api/legal/termos").retrieve().body(LegalDocument.class);
    }

    public LegalDocument getPrivacyPolicy() {
        return restClient.get().uri("/api/legal/privacidade").retrieve().body(LegalDocument.class);
    }
}
