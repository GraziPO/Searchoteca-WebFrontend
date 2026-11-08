package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.config.BackendMessages;
import searchoteca.frontend.service.BackendService;

/** Conta do usuário logado. */
@Controller
public class AccountController {

    private final BackendService backendService;

    public AccountController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping("/conta/senha")
    public String changePasswordForm(Model model) {
        model.addAttribute("dica", "A senha deve ter ao menos 8 caracteres, com letras e números.");
        return "conta_senha";
    }

    @PostMapping("/conta/senha")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("erro", "As duas senhas novas não são iguais.");
            return "redirect:/conta/senha";
        }
        try {
            backendService.changeOwnPassword(currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("info", "Senha alterada.");
        } catch (RestClientException ex) {
            // Senha atual errada volta 409 (e não 401), então fica na tela
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível alterar a senha."));
        }
        return "redirect:/conta/senha";
    }
}
