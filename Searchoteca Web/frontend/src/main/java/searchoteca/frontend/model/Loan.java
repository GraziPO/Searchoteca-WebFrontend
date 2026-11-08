package searchoteca.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Espelha searchoteca.model.LoanModel. O backend ainda não devolve os itens do empréstimo. */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Loan {
    private Long loanId;
    private LocalDateTime loanDate;
    private LocalDate dueDate;
    private Long operatorId;
    private Long studentId;
    private String status;

    public Loan() {}

    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    /** Calculado na tela: aberto e com a data prevista já passada. */
    public boolean isOverdue() {
        return isOpen() && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public String getStatusLabel() {
        if (isOverdue()) return "Atrasado";
        return switch (status == null ? "" : status) {
            case "OPEN" -> "Aberto";
            case "CLOSED" -> "Devolvido";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }
}
