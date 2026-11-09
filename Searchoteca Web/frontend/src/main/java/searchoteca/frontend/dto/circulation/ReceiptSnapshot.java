package searchoteca.frontend.dto.circulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Comprovante de saída, exatamente como o backend gravou. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceiptSnapshot(String code, String exitType, LocalDate issuedAt,
                              Operator operator, Student student, LocalDate dueDate,
                              List<Item> items, BigDecimal total, String notes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Operator(Long id, String username, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Student(String ra, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Long copyId, String copyCode, String isbn, String title,
                       String author, BigDecimal unitPrice) {}

    public String exitTypeLabel() {
        return ExitTypes.label(exitType);
    }
}
