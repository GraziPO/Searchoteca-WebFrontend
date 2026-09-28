package searchoteca.frontend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.dto.AuthResult;
import searchoteca.frontend.dto.LegalDocument;
import searchoteca.frontend.service.AuthService;

import java.util.Map;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /* ---------------- ETAPA 1: usuário e senha ---------------- */

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("jwt") != null) {
            return "redirect:/acervo";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                         @RequestParam String password,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        clearPendingLogin(session);
        try {
            AuthResult result = authService.login(username, password);
            return nextStep(result, session, redirectAttributes);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Usuário ou senha inválidos."));
            return "redirect:/login";
        }
    }

    /* ---------------- ETAPA 2: aceite dos termos ---------------- */

    @GetMapping("/termos")
    public String termsForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("pendingTermsToken") == null) {
            return "redirect:/login";
        }
        try {
            LegalDocument terms = authService.getTerms();
            LegalDocument privacy = authService.getPrivacyPolicy();
            // Guarda as versões que o usuário está VENDO: é isso que vai no aceite
            session.setAttribute("shownTermsVersion", terms.version());
            session.setAttribute("shownPrivacyVersion", privacy.version());
            model.addAttribute("termos", terms);
            model.addAttribute("privacidade", privacy);
            return "termos";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro",
                    errorMessage(ex, "Não foi possível carregar os Termos de Uso agora."));
            return "redirect:/login";
        }
    }

    @PostMapping("/termos/aceitar")
    public String acceptTerms(@RequestParam(name = "aceito", required = false) String aceito,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String termsToken = (String) session.getAttribute("pendingTermsToken");
        if (termsToken == null) {
            return "redirect:/login";
        }
        if (aceito == null) {
            redirectAttributes.addFlashAttribute("erro",
                    "Para continuar, marque que leu e aceita os Termos de Uso e a Política de Privacidade.");
            return "redirect:/termos";
        }

        try {
            AuthResult result = authService.acceptTerms(termsToken,
                    (String) session.getAttribute("shownTermsVersion"),
                    (String) session.getAttribute("shownPrivacyVersion"));
            session.removeAttribute("pendingTermsToken");
            session.removeAttribute("shownTermsVersion");
            session.removeAttribute("shownPrivacyVersion");
            return nextStep(result, session, redirectAttributes);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 409) {
                // Documentos mudaram enquanto o usuário lia: mostra a versão nova
                redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Os documentos foram atualizados."));
                return "redirect:/termos";
            }
            clearPendingLogin(session);
            redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Sessão expirada. Faça login novamente."));
            return "redirect:/login";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Não foi possível registrar o aceite."));
            return "redirect:/termos";
        }
    }

    /* Leitura pública dos documentos (link na tela de login) */

    @GetMapping("/termos-de-uso")
    public String publicTerms(Model model, RedirectAttributes redirectAttributes) {
        return showDocument(true, model, redirectAttributes);
    }

    @GetMapping("/politica-de-privacidade")
    public String publicPrivacy(Model model, RedirectAttributes redirectAttributes) {
        return showDocument(false, model, redirectAttributes);
    }

    /* ---------------- ETAPA 3: código do MFA ---------------- */

    @GetMapping("/mfa")
    public String mfaForm(HttpSession session, Model model) {
        if (session.getAttribute("pendingMfaToken") == null) {
            return "redirect:/login";
        }
        model.addAttribute("email", session.getAttribute("pendingEmail"));
        return "mfa";
    }

    @PostMapping("/mfa/verify")
    public String verifyMfa(@RequestParam String code,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String mfaToken = (String) session.getAttribute("pendingMfaToken");
        if (mfaToken == null) {
            return "redirect:/login";
        }

        try {
            AuthResult result = authService.verifyMfa(mfaToken, code);
            clearPendingLogin(session);
            startSession(session, result);
            return "redirect:/acervo";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Código inválido ou expirado."));
            return "redirect:/mfa";
        }
    }

    @PostMapping("/mfa/resend")
    public String resendMfa(HttpSession session, RedirectAttributes redirectAttributes) {
        String mfaToken = (String) session.getAttribute("pendingMfaToken");
        if (mfaToken == null) {
            return "redirect:/login";
        }

        try {
            AuthResult result = authService.resendMfa(mfaToken);
            if (result.getMfaToken() != null) {
                session.setAttribute("pendingMfaToken", result.getMfaToken());
            }
            if (result.getEmail() != null) {
                session.setAttribute("pendingEmail", result.getEmail());
            }
            redirectAttributes.addFlashAttribute("info", "Um novo código foi enviado.");
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro",
                    errorMessage(ex, "Não foi possível reenviar o código agora. Tente novamente em instantes."));
        }
        return "redirect:/mfa";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    /* ---------------- auxiliares ---------------- */

    /** Decide a próxima tela a partir da resposta do backend (login ou aceite). */
    private String nextStep(AuthResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        if (result == null) {
            redirectAttributes.addFlashAttribute("erro", "O servidor não respondeu ao login. Tente novamente.");
            return "redirect:/login";
        }
        if (result.isFullyAuthenticated()) {
            startSession(session, result);
            return "redirect:/acervo";
        }
        if (result.isTermsChallenge()) {
            session.setAttribute("pendingTermsToken", result.getTermsToken());
            return "redirect:/termos";
        }
        if (result.isMfaChallenge()) {
            session.setAttribute("pendingMfaToken", result.getMfaToken());
            session.setAttribute("pendingEmail", result.getEmail());
            return "redirect:/mfa";
        }
        log.warn("Resposta de login em formato inesperado: nenhum token, termsToken ou mfaToken");
        redirectAttributes.addFlashAttribute("erro", "Não foi possível entrar. Tente novamente.");
        return "redirect:/login";
    }

    private String showDocument(boolean terms, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("documento", terms ? authService.getTerms() : authService.getPrivacyPolicy());
            return "documento_legal";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", errorMessage(ex, "Documento indisponível no momento."));
            return "redirect:/login";
        }
    }

    /**
     * Mostra a mensagem real do problema em vez de sempre "usuário ou senha inválidos":
     * backend fora do ar, mensagem de erro do backend, ou o texto padrão.
     */
    private String errorMessage(RestClientException ex, String fallback) {
        if (ex instanceof ResourceAccessException) {
            log.error("Backend inacessível: {}", ex.getMessage());
            return "Não foi possível conectar ao servidor. Verifique se o backend está rodando.";
        }
        if (ex instanceof RestClientResponseException rre) {
            log.warn("Backend respondeu HTTP {}: {}", rre.getStatusCode().value(), rre.getResponseBodyAsString());
            try {
                Map<?, ?> body = rre.getResponseBodyAs(Map.class);
                if (body != null && body.get("message") instanceof String message && !message.isBlank()) {
                    return message;
                }
            } catch (Exception ignored) {
                // corpo não é JSON: usa o texto padrão
            }
            return fallback;
        }
        log.error("Falha ao chamar o backend", ex);
        return fallback;
    }

    private void clearPendingLogin(HttpSession session) {
        session.removeAttribute("pendingTermsToken");
        session.removeAttribute("shownTermsVersion");
        session.removeAttribute("shownPrivacyVersion");
        session.removeAttribute("pendingMfaToken");
        session.removeAttribute("pendingEmail");
    }

    private void startSession(HttpSession session, AuthResult result) {
        session.setAttribute("jwt", result.getToken());
        session.setAttribute("username", result.getUsername());
        session.setAttribute("role", result.getRole_code());
    }
}
