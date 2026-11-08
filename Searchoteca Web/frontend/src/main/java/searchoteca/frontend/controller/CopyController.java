package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.config.BackendMessages;
import searchoteca.frontend.dto.archive.BatchMoveRequest;
import searchoteca.frontend.dto.archive.BatchMoveResult;
import searchoteca.frontend.dto.archive.CopyLabel;
import searchoteca.frontend.dto.archive.CopyRequest;
import searchoteca.frontend.service.BackendService;

import java.util.List;

/** Exemplares: cadastro em quantidade, etiquetas e movimentação em lote. */
@Controller
public class CopyController {

    private final BackendService backendService;

    public CopyController(BackendService backendService) {
        this.backendService = backendService;
    }

    /* ---------------- cadastro ---------------- */

    @GetMapping("/acervo/exemplares/novo")
    public String newCopiesForm(@RequestParam(required = false) String isbn, Model model) {
        model.addAttribute("isbn", isbn);
        model.addAttribute("livros", backendService.listBooks());
        model.addAttribute("localizacoes", backendService.listLocal());
        return "search_exemplares";
    }

    @PostMapping("/acervo/exemplares")
    public String createCopies(@RequestParam String isbn,
                               @RequestParam String localCode,
                               @RequestParam int quantity,
                               RedirectAttributes redirectAttributes) {
        try {
            // O departamento vem da localização escolhida: o usuário não precisa informar os dois
            String departCode = backendService.getLocal(localCode).getDepartCode();
            List<CopyLabel> labels = backendService.createCopies(new CopyRequest(isbn, departCode, localCode, quantity));
            // Redirect depois do POST: um F5 na página de etiquetas não cadastra tudo de novo
            redirectAttributes.addFlashAttribute("etiquetas", labels);
            redirectAttributes.addFlashAttribute("titulo", labels.size() + " exemplar(es) cadastrado(s)");
            redirectAttributes.addFlashAttribute("info", "Imprima as etiquetas e cole nos livros antes de colocá-los na prateleira.");
            return "redirect:/acervo/exemplares/impressao";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível cadastrar os exemplares."));
            return "redirect:/acervo/exemplares/novo?isbn=" + isbn;
        }
    }

    /* ---------------- etiquetas ---------------- */

    /** Sem ids: formulário de reimpressão. Com ids: página de impressão. */
    @GetMapping("/acervo/exemplares/etiquetas")
    public String labels(@RequestParam(required = false) String ids, Model model, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isBlank()) {
            return "reimprimir_etiquetas";
        }
        try {
            model.addAttribute("etiquetas", backendService.findLabels(FormParsing.ids(ids)));
            model.addAttribute("titulo", "Reimpressão de etiquetas");
            return "etiquetas";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível carregar as etiquetas."));
        }
        redirectAttributes.addFlashAttribute("ids", ids);
        return "redirect:/acervo/exemplares/etiquetas";
    }

    /** Destino do redirect do cadastro e da movimentação: as etiquetas chegam por flash attribute. */
    @GetMapping("/acervo/exemplares/impressao")
    public String printAfterChange(Model model) {
        if (!model.containsAttribute("etiquetas")) {
            return "redirect:/acervo/exemplares/etiquetas";
        }
        return "etiquetas";
    }

    /* ---------------- movimentação em lote ---------------- */

    @GetMapping("/acervo/exemplares/movimentacao")
    public String moveForm(Model model) {
        model.addAttribute("localizacoes", backendService.listLocal());
        return "movimentacao";
    }

    @PostMapping("/acervo/exemplares/movimentacao")
    public String move(@RequestParam String localCode,
                       @RequestParam String copyCodes,
                       @RequestParam(required = false) Integer expectedQuantity,
                       RedirectAttributes redirectAttributes) {
        try {
            BatchMoveResult result = backendService.moveCopies(
                    new BatchMoveRequest(localCode, FormParsing.codes(copyCodes), expectedQuantity));
            redirectAttributes.addFlashAttribute("resultado", result);
            redirectAttributes.addFlashAttribute("etiquetas", result.labels());
            redirectAttributes.addFlashAttribute("titulo", result.moved() + " exemplar(es) movido(s) para " + result.localCode());
            redirectAttributes.addFlashAttribute("info", result.moved() == 0
                    ? "Nenhum exemplar precisou ser movido."
                    : "Se o código do exemplar inclui a localização, reimprima as etiquetas abaixo.");
            return "redirect:/acervo/exemplares/impressao";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível movimentar os exemplares."));
            // Devolve a leitura para o operador não precisar ler tudo de novo
            redirectAttributes.addFlashAttribute("localCode", localCode);
            redirectAttributes.addFlashAttribute("copyCodes", copyCodes);
            redirectAttributes.addFlashAttribute("expectedQuantity", expectedQuantity);
            return "redirect:/acervo/exemplares/movimentacao";
        }
    }
}
