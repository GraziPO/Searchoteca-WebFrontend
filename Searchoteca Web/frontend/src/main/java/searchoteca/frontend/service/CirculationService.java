package searchoteca.frontend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.dto.circulation.ExitRequest;
import searchoteca.frontend.dto.circulation.LoanRequest;
import searchoteca.frontend.dto.circulation.ReceiptSnapshot;
import searchoteca.frontend.model.Loan;

import java.util.List;

/** Empréstimos, devoluções, saídas definitivas e comprovantes. */
@Service
public class CirculationService {

    /**
     * No backend, a rota do comprovante está declarada dentro do ExitController,
     * que tem @RequestMapping("/api/saidas"). Por isso o caminho real hoje é
     * /api/saidas/api/comprovantes/{code}. Quando o backend for corrigido para
     * /api/comprovantes/{code}, basta trocar esta constante.
     */
    private static final String RECEIPT_PATH = "/api/saidas/api/comprovantes/{code}";

    private final RestClient restClient;

    public CirculationService(RestClient backendRestClient) {
        this.restClient = backendRestClient;
    }

    // ---------------- EMPRÉSTIMOS ----------------

    public List<Loan> listLoans() {
        return restClient.get()
                .uri("/api/emprestimos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Loan>>() {});
    }

    /** Empréstimos do aluno logado (loans:view_own). */
    public List<Loan> listMyLoans() {
        return restClient.get()
                .uri("/api/emprestimos/historico")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Loan>>() {});
    }

    public Loan createLoan(LoanRequest request) {
        return restClient.post()
                .uri("/api/emprestimos")
                .body(request)
                .retrieve()
                .body(Loan.class);
    }

    public Loan cancelLoan(Long loanId) {
        return restClient.post()
                .uri("/api/emprestimos/{id}/cancelar", loanId)
                .retrieve()
                .body(Loan.class);
    }

    /** A devolução é pelo exemplar: o backend acha o empréstimo em aberto dele. */
    public Loan returnCopy(Long copyId) {
        return restClient.post()
                .uri("/api/emprestimos/devolucao/{copyId}", copyId)
                .retrieve()
                .body(Loan.class);
    }

    // ---------------- SAÍDAS E COMPROVANTES ----------------

    public ReceiptSnapshot registerExit(ExitRequest request) {
        return restClient.post()
                .uri("/api/saidas")
                .body(request)
                .retrieve()
                .body(ReceiptSnapshot.class);
    }

    public ReceiptSnapshot findReceipt(String code) {
        return restClient.get()
                .uri(RECEIPT_PATH, code.trim())
                .retrieve()
                .body(ReceiptSnapshot.class);
    }
}
