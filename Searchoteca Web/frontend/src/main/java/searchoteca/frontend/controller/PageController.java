package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import searchoteca.frontend.model.Book;
import searchoteca.frontend.model.Department;
import searchoteca.frontend.model.Location;
import searchoteca.frontend.service.BackendService;

@Controller
public class PageController {

    private final BackendService backendService;

    public PageController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/acervo")
    public String archive() {
        return "home_acervos";
    }

    /* ---------------- LIVROS ---------------- */

    @GetMapping("/acervo/livros")
    public String listBooks(Model model) {
        model.addAttribute("livros", backendService.listBooks());
        return "home_livros";
    }

    @GetMapping("/acervo/livros/novo")
    public String newBookForm(Model model) {
        model.addAttribute("livro", new Book());
        model.addAttribute("editando", false);
        return "search_livros";
    }

    @PostMapping("/acervo/livros")
    public String createBook(@ModelAttribute("livro") Book book) {
        backendService.createBook(book);
        return "redirect:/acervo/livros";
    }

    @GetMapping("/acervo/livros/{isbn}/editar")
    public String editBookForm(@PathVariable String isbn, Model model) {
        model.addAttribute("livro", backendService.getBook(isbn));
        model.addAttribute("editando", true);
        return "search_livros";
    }

    @PostMapping("/acervo/livros/{isbn}/editar")
    public String updateBook(@PathVariable String isbn, @ModelAttribute("livro") Book book) {
        backendService.updateBook(isbn, book);
        return "redirect:/acervo/livros";
    }

    @PostMapping("/acervo/livros/{isbn}/excluir")
    public String deleteBook(@PathVariable String isbn) {
        backendService.deleteBook(isbn);
        return "redirect:/acervo/livros";
    }

    /* ---------------- DEPARTAMENTOS ---------------- */

    @GetMapping("/acervo/departamentos")
    public String listDepart(Model model) {
        model.addAttribute("departamentos", backendService.listDepart());
        return "home_depart";
    }

    @GetMapping("/acervo/departamentos/novo")
    public String newDepartForm(Model model) {
        model.addAttribute("departamento", new Department());
        model.addAttribute("editando", false);
        return "search_depart";
    }

    @PostMapping("/acervo/departamentos")
    public String createDepart(@ModelAttribute("departamento") Department depart) {
        backendService.createDepart(depart);
        return "redirect:/acervo/departamentos";
    }

    @GetMapping("/acervo/departamentos/{departCode}/editar")
    public String editDepartForm(@PathVariable String departCode, Model model) {
        model.addAttribute("departamento", backendService.getDepart(departCode));
        model.addAttribute("editando", true);
        return "search_depart";
    }

    @PostMapping("/acervo/departamentos/{departCode}/editar")
    public String updateDepart(@PathVariable String departCode, @ModelAttribute("departamento") Department depart) {
        backendService.updateDepart(departCode, depart);
        return "redirect:/acervo/departamentos";
    }

    @PostMapping("/acervo/departamentos/{departCode}/excluir")
    public String deleteDepart(@PathVariable String departCode) {
        backendService.deleteDepart(departCode);
        return "redirect:/acervo/departamentos";
    }

    /* ---------------- LOCALIZACOES ---------------- */

    @GetMapping("/acervo/localizacoes")
    public String listLocal(Model model) {
        model.addAttribute("localizacoes", backendService.listLocal());
        return "home_local";
    }

    @GetMapping("/acervo/localizacoes/novo")
    public String newLocalForm(Model model) {
        model.addAttribute("localizacao", new Location());
        model.addAttribute("editando", false);
        return "search_local";
    }

    @PostMapping("/acervo/localizacoes")
    public String createLocal(@ModelAttribute("localizacao") Location local) {
        backendService.createLocal(local);
        return "redirect:/acervo/localizacoes";
    }

    @GetMapping("/acervo/localizacoes/{localCode}/editar")
    public String editLocalForm(@PathVariable String localCode, Model model) {
        model.addAttribute("localizacao", backendService.getLocal(localCode));
        model.addAttribute("editando", true);
        return "search_local";
    }

    @PostMapping("/acervo/localizacoes/{localCode}/editar")
    public String updateLocal(@PathVariable String localCode, @ModelAttribute("localizacao") Location local) {
        backendService.updateLocal(localCode, local);
        return "redirect:/acervo/localizacoes";
    }

    @PostMapping("/acervo/localizacoes/{localCode}/excluir")
    public String deleteLocal(@PathVariable String localCode) {
        backendService.deleteLocal(localCode);
        return "redirect:/acervo/localizacoes";
    }
}
