package searchoteca.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import searchoteca.frontend.config.BackendMessages;
import searchoteca.frontend.dto.circulation.ExitRequest;
import searchoteca.frontend.dto.circulation.ExitTypes;
import searchoteca.frontend.dto.circulation.LoanRequest;
import searchoteca.frontend.dto.circulation.ReceiptSnapshot;
import searchoteca.frontend.model.Loan;
import searchoteca.frontend.service.CirculationService;

import java.util.Comparator;
import java.util.List;

/** Empréstimos, devoluções, saídas definitivas e comprovantes. */
@Controller
public class CirculationController {

    private final CirculationService circulationService;

    public CirculationController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    /* ---------------- empréstimos ---------------- */

    /** status: OPEN, OVERDUE, CLOSED, CANCELLED ou vazio (todos). O filtro é feito aqui: o backend ainda não filtra. */
    @GetMapping("/emprestimos")
    public String listLoans(@RequestParam(required = false) String status, Model model) {
        List<Loan> loans = circulationService.listLoans().stream()
                .filter(l -> matches(l, status))
                .sorted(Comparator.comparing(Loan::getLoanDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("emprestimos", loans);
        model.addAttribute("status", status == null ? "" : status);
        return "home_emprestimos";
    }

    @GetMapping("/emprestimos/novo")
    public String newLoanForm() {
        return "search_emprestimos";
    }

    @PostMapping("/emprestimos")
    public String createLoan(@RequestParam String ra,
                             @RequestParam String items,
                             RedirectAttributes redirectAttributes) {
        try {
            Loan loan = circulationService.createLoan(new LoanRequest(ra.trim(), FormParsing.ids(items)));
            redirectAttributes.addFlashAttribute("info", "Empréstimo " + loan.getLoanId()
                    + " registrado. Devolução prevista para " + formatDate(loan) + ".");
            return "redirect:/emprestimos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível registrar o empréstimo."));
        }
        redirectAttributes.addFlashAttribute("ra", ra);
        redirectAttributes.addFlashAttribute("items", items);
        return "redirect:/emprestimos/novo";
    }

    @PostMapping("/emprestimos/{id}/cancelar")
    public String cancelLoan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            circulationService.cancelLoan(id);
            redirectAttributes.addFlashAttribute("info", "Empréstimo " + id + " cancelado. Os exemplares voltaram para o acervo.");
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível cancelar o empréstimo."));
        }
        return "redirect:/emprestimos";
    }

    @PostMapping("/emprestimos/devolucao")
    public String returnCopy(@RequestParam String copyId, RedirectAttributes redirectAttributes) {
        try {
            Long id = FormParsing.ids(copyId).stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Informe o ID do exemplar."));
            Loan loan = circulationService.returnCopy(id);
            redirectAttributes.addFlashAttribute("info", "CLOSED".equals(loan.getStatus())
                    ? "Exemplar " + id + " devolvido. O empréstimo " + loan.getLoanId() + " foi encerrado."
                    : "Exemplar " + id + " devolvido. O empréstimo " + loan.getLoanId() + " ainda tem livros pendentes.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível registrar a devolução."));
        }
        return "redirect:/emprestimos";
    }

    /** Área do aluno. */
    @GetMapping("/meus-emprestimos")
    public String myLoans(Model model) {
        List<Loan> loans = circulationService.listMyLoans().stream()
                .sorted(Comparator.comparing(Loan::getLoanDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("emprestimos", loans);
        return "meus_emprestimos";
    }

    /* ---------------- saídas definitivas ---------------- */

    @GetMapping("/saidas/nova")
    public String newExitForm(Model model) {
        model.addAttribute("tipos", ExitTypes.DEFINITIVE);
        return "search_saidas";
    }

    @PostMapping("/saidas")
    public String registerExit(@RequestParam String type,
                               @RequestParam String copyIds,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            ReceiptSnapshot receipt = circulationService.registerExit(
                    new ExitRequest(type, FormParsing.ids(copyIds), notes));
            redirectAttributes.addFlashAttribute("comprovante", receipt);
            redirectAttributes.addFlashAttribute("info", "Saída registrada.");
            return "redirect:/comprovantes/emitido";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("erro", BackendMessages.forForm(ex, "Não foi possível registrar a saída."));
        }
        redirectAttributes.addFlashAttribute("type", type);
        redirectAttributes.addFlashAttribute("copyIds", copyIds);
        redirectAttributes.addFlashAttribute("notes", notes);
        return "redirect:/saidas/nova";
    }

    /* ---------------- comprovantes ---------------- */

    /** Busca pelo código (ex.: CMP-20261107-000042). */
    @GetMapping("/comprovantes")
    public String findReceipt(@RequestParam(required = false) String codigo, Model model) {
        if (codigo != null && !codigo.isBlank()) {
            try {
                model.addAttribute("comprovante", circulationService.findReceipt(codigo));
            } catch (RestClientException ex) {
                model.addAttribute("erro", BackendMessages.forForm(ex, "Comprovante não encontrado."));
            }
        }
        model.addAttribute("codigo", codigo);
        return "comprovante";
    }

    /** Logo depois de uma saída: o comprovante chega por flash attribute. */
    @GetMapping("/comprovantes/emitido")
    public String issuedReceipt(Model model) {
        if (!model.containsAttribute("comprovante")) {
            return "redirect:/comprovantes";
        }
        return "comprovante";
    }

    /* ---------------- auxiliares ---------------- */

    private static boolean matches(Loan loan, String status) {
        if (status == null || status.isBlank()) return true;
        if ("OVERDUE".equals(status)) return loan.isOverdue();
        return status.equals(loan.getStatus());
    }

    private static String formatDate(Loan loan) {
        if (loan.getDueDate() == null) return "-";
        return loan.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
