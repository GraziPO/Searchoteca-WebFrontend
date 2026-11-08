package searchoteca.frontend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * O Thymeleaf 3.1 (Spring Boot 3+) não expõe mais ${session} nos templates.
 * Este advice coloca no model de toda página quem está logado e o que o perfil pode ver.
 */
@ControllerAdvice
public class SessionModelAdvice {

    @ModelAttribute
    public void addCurrentUser(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String username = session == null ? null : (String) session.getAttribute("username");
        String role = session == null ? null : (String) session.getAttribute("role");

        model.addAttribute("currentUser", username);
        model.addAttribute("currentRoleLabel", RoleAccess.label(role));
        model.addAttribute("nav", RoleAccess.of(role));
        model.addAttribute("roleLabels", RoleAccess.LABELS);
    }
}
