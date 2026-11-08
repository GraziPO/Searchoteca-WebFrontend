package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import searchoteca.frontend.service.BackendService;

/** Relatórios. Por enquanto, a trilha de auditoria (audit:view, só root). */
@Controller
public class ReportController {

    private final BackendService backendService;

    public ReportController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping("/relatorios/auditoria")
    public String audit(@RequestParam(required = false) String usuario,
                        @RequestParam(defaultValue = "100") int limite,
                        Model model) {
        int limit = Math.min(Math.max(limite, 1), 500);
        model.addAttribute("registros", backendService.listAudit(usuario, limit));
        model.addAttribute("usuario", usuario);
        model.addAttribute("limite", limit);
        return "auditoria";
    }
}
