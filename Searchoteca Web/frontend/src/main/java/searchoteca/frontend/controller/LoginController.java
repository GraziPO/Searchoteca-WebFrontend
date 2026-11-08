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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.config.BackendMessages;
import searchoteca.frontend.config.RoleAccess;
import searchoteca.frontend.dto.AuthResult;
import searchoteca.frontend.dto.LegalDocument;
import searchoteca.frontend.service.AuthService;

/**
 * Login em até quatro etapas, na mesma ordem do backend:
 *   1. /login            usuário e senha
 *   2. /termos           aceite dos Termos e da Política (se a versão atual não foi aceita)
 *   3. /mfa              código enviado por e-mail
 *   4. /primeiro-acesso  troca da senha inicial (se o usuário foi cadastrado por outra pessoa)
 * Também cuida do "esqueci a senha", que não exige login.
 */
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private static final String PASSWORD_HINT = "A senha deve ter ao menos 8 caracteres, com letras e números.";

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /* ---------------- página inicial ---------------- */

    /** "/" é público: quem está logado vai para a página do próprio perfil; os demais, para o login. */
    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("jwt") == null) {
            return "redirect:/login";
        }
        return "redirect:" + RoleAccess.of((String) session.getAttribute("role")).homePath();
    }

    /* ---------------- ETAPA 1: usuário e senha ---------------- */

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("jwt") != null) {
            return "redirect:/";
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
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Usuário ou senha inválidos."));
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
                    BackendMessages.of(ex, "Não foi possível carregar os Termos de Uso agora."));
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
                redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Os documentos foram atualizados."));
                return "redirect:/termos";
            }
            clearPendingLogin(session);
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Sessão expirada. Faça login novamente."));
            return "redirect:/login";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Não foi possível registrar o aceite."));
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
            session.removeAttribute("pendingMfaToken");
            session.removeAttribute("pendingEmail");
            // Antes ia direto para a sessão; agora o backend pode pedir a troca de senha
            return nextStep(result, session, redirectAttributes);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Código inválido ou expirado."));
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
                    BackendMessages.of(ex, "Não foi possível reenviar o código agora. Tente novamente em instantes."));
        }
        return "redirect:/mfa";
    }

    /* ---------------- ETAPA 4: troca da senha no primeiro acesso ---------------- */

    @GetMapping("/primeiro-acesso")
    public String firstAccessForm(HttpSession session, Model model) {
        if (session.getAttribute("pendingPasswordChangeToken") == null) {
            return "redirect:/login";
        }
        model.addAttribute("dica", PASSWORD_HINT);
        return "primeiro_acesso";
    }

    @PostMapping("/primeiro-acesso")
    public String firstAccess(@RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("pendingPasswordChangeToken");
        if (token == null) {
            return "redirect:/login";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("erro", "As duas senhas não são iguais.");
            return "redirect:/primeiro-acesso";
        }
        try {
            AuthResult result = authService.completeFirstAccess(token, newPassword);
            session.removeAttribute("pendingPasswordChangeToken");
            return nextStep(result, session, redirectAttributes);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                // Token de troca expirou (15 min): recomeça o login
                clearPendingLogin(session);
                redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Sessão expirada. Faça login novamente."));
                return "redirect:/login";
            }
            // 409: senha fora do padrão ou igual à atual; continua na mesma tela
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Não foi possível trocar a senha."));
            return "redirect:/primeiro-acesso";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Não foi possível trocar a senha."));
            return "redirect:/primeiro-acesso";
        }
    }

    /* ---------------- esqueci a senha ---------------- */

    @GetMapping("/esqueci-senha")
    public String forgotPasswordForm() {
        return "esqueci_senha";
    }

    @PostMapping("/esqueci-senha")
    public String forgotPassword(@RequestParam String login, RedirectAttributes redirectAttributes) {
        try {
            authService.requestPasswordReset(login);
            // A mensagem é a mesma exista o usuário ou não: não revela quem está cadastrado
            redirectAttributes.addFlashAttribute("info",
                    "Se o usuário existir e estiver ativo, enviamos um código para o e-mail cadastrado.");
            redirectAttributes.addFlashAttribute("login", login);
            return "redirect:/redefinir-senha";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Não foi possível enviar o código agora."));
            return "redirect:/esqueci-senha";
        }
    }

    @GetMapping("/redefinir-senha")
    public String resetPasswordForm(Model model) {
        model.addAttribute("dica", PASSWORD_HINT);
        return "redefinir_senha";
    }

    @PostMapping("/redefinir-senha")
    public String resetPassword(@RequestParam String login,
                                @RequestParam String code,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("erro", "As duas senhas não são iguais.");
            redirectAttributes.addFlashAttribute("login", login);
            return "redirect:/redefinir-senha";
        }
        try {
            authService.resetPassword(login, code, newPassword);
            redirectAttributes.addFlashAttribute("info", "Senha redefinida. Entre com a nova senha.");
            return "redirect:/login";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Código inválido ou expirado."));
            redirectAttributes.addFlashAttribute("login", login);
            return "redirect:/redefinir-senha";
        }
    }

    /* ---------------- sair ---------------- */

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    /* ---------------- auxiliares ---------------- */

    /** Decide a próxima tela a partir da resposta do backend (login, aceite, MFA ou troca de senha). */
    private String nextStep(AuthResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        if (result == null) {
            redirectAttributes.addFlashAttribute("erro", "O servidor não respondeu ao login. Tente novamente.");
            return "redirect:/login";
        }
        if (result.isFullyAuthenticated()) {
            startSession(session, result);
            return "redirect:" + RoleAccess.of(result.getRole_code()).homePath();
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
        if (result.isPasswordChangeChallenge()) {
            session.setAttribute("pendingPasswordChangeToken", result.getPasswordChangeToken());
            return "redirect:/primeiro-acesso";
        }
        log.warn("Resposta de login em formato inesperado: nenhum token, termsToken, mfaToken ou passwordChangeToken");
        redirectAttributes.addFlashAttribute("erro", "Não foi possível entrar. Tente novamente.");
        return "redirect:/login";
    }

    private String showDocument(boolean terms, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("documento", terms ? authService.getTerms() : authService.getPrivacyPolicy());
            return "documento_legal";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.of(ex, "Documento indisponível no momento."));
            return "redirect:/login";
        }
    }

    private void clearPendingLogin(HttpSession session) {
        session.removeAttribute("pendingTermsToken");
        session.removeAttribute("shownTermsVersion");
        session.removeAttribute("shownPrivacyVersion");
        session.removeAttribute("pendingMfaToken");
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPasswordChangeToken");
    }

    private void startSession(HttpSession session, AuthResult result) {
        session.setAttribute("jwt", result.getToken());
        session.setAttribute("username", result.getUsername());
        session.setAttribute("role", result.getRole_code());
    }
}
