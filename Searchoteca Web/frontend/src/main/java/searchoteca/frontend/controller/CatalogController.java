package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import searchoteca.frontend.service.CatalogService;

/** Consulta pública ao acervo: funciona sem login. */
@Controller
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/consulta")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("q", q);
        model.addAttribute("resultado", catalogService.search(q, page));
        return "consulta";
    }

    @GetMapping("/consulta/{isbn}")
    public String detail(@PathVariable String isbn,
                         @RequestParam(defaultValue = "") String q,
                         Model model) {
        model.addAttribute("q", q);
        model.addAttribute("livro", catalogService.detail(isbn));
        return "consulta_detalhe";
    }
}
