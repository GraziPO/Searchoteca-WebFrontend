package searchoteca.frontend.dto.circulation;

import java.util.List;

/** Corpo de POST /api/emprestimos: RA do aluno e ids dos exemplares. */
public record LoanRequest(String ra, List<Long> items) {}
