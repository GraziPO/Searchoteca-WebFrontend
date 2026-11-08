package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.config.BackendMessages;
import searchoteca.frontend.dto.users.StudentRequest;
import searchoteca.frontend.model.Book;
import searchoteca.frontend.model.Department;
import searchoteca.frontend.model.Location;
import searchoteca.frontend.model.User;
import searchoteca.frontend.service.BackendService;

import java.math.BigDecimal;

@Controller
public class PageController {

    private final BackendService backendService;

    public PageController(BackendService backendService) {
        this.backendService = backendService;
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

    /** Preço de venda: formulário separado na tela de edição, visível só para o root. */
    @PostMapping("/acervo/livros/{isbn}/preco")
    public String updateBookPrice(@PathVariable String isbn,
                                  @RequestParam(required = false) BigDecimal salePrice,
                                  RedirectAttributes redirectAttributes) {
        try {
            backendService.updateBookPrice(isbn, salePrice);
            redirectAttributes.addFlashAttribute("info", salePrice == null
                    ? "Preço removido."
                    : "Preço atualizado para R$ " + salePrice.setScale(2, java.math.RoundingMode.HALF_UP) + ".");
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível alterar o preço."));
        }
        return "redirect:/acervo/livros/" + isbn + "/editar";
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
        model.addAttribute("departamentos", backendService.listDepart());
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
        model.addAttribute("departamentos", backendService.listDepart());
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

    /* ---------------- USUARIOS ---------------- */

    @GetMapping("/usuarios")
    public String listUsers(Model model) {
        model.addAttribute("usuarios", backendService.listUsers());
        return "home_usuarios";
    }

    @GetMapping("/usuarios/novo")
    public String newUserForm(Model model) {
        model.addAttribute("usuario", new User());
        model.addAttribute("editando", false);
        return "search_usuarios";
    }

    @PostMapping("/usuarios")
    public String createUser(@ModelAttribute("usuario") User user) {
        backendService.createUser(user);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/{username}/editar")
    public String editUserForm(@PathVariable String username, Model model) {
        User user = backendService.getUser(username);
        user.setPassword(null); // nunca preenche a senha de volta no formulario
        model.addAttribute("usuario", user);
        model.addAttribute("editando", true);
        return "search_usuarios";
    }

    @PostMapping("/usuarios/{username}/editar")
    public String updateUser(@PathVariable String username, @ModelAttribute("usuario") User user) {
        backendService.updateUser(username, user);
        return "redirect:/usuarios";
    }

    /* ---------------- ALUNOS ---------------- */

    @GetMapping("/alunos/novo")
    public String newStudentForm() {
        return "search_alunos";
    }

    @PostMapping("/alunos")
    public String createStudent(@RequestParam String username,
                                @RequestParam String completeName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String ra,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        try {
            backendService.createStudent(new StudentRequest(username, completeName, email, password, ra, phone));
            redirectAttributes.addFlashAttribute("info",
                    "Aluno " + completeName + " cadastrado. No primeiro acesso, ele vai precisar trocar a senha.");
            return "redirect:/alunos/novo";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível cadastrar o aluno."));
            redirectAttributes.addFlashAttribute("aluno",
                    new StudentRequest(username, completeName, email, null, ra, phone));
            return "redirect:/alunos/novo";
        }
    }

    @PostMapping("/usuarios/{username}/ativar")
    public String activateUser(@PathVariable String username) {
        backendService.activateUser(username);
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{username}/desativar")
    public String deactivateUser(@PathVariable String username) {
        backendService.deactivateUser(username);
        return "redirect:/usuarios";
    }
}
