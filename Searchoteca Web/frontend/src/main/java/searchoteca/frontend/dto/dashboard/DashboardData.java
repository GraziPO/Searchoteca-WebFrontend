package searchoteca.frontend.dto.dashboard;

import searchoteca.frontend.dto.AuditLog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta de GET /api/dashboard (DashboardResponse do backend).
 * Os nomes dos campos são os mesmos dos records do backend, para o Jackson casar sozinho.
 * Campos que o backend não mandar chegam como null/0 (o Spring Boot não falha em propriedade desconhecida).
 */
public record DashboardData(
        int periodDays,
        LocalDateTime generatedAt,
        Kpis kpis,
        List<Diary> diary,
        List<CountByStatus> copiesByStatus,
        List<DepartmentOccupation> departmentOccupation,
        List<DelayedLoan> delayedLoans,
        SecuritySummary security,
        List<AuditLog> recentActivity) {

    public record Kpis(long openLoans,
                       long delayedLoans,
                       long activeStudents,
                       long inactiveStudents,
                       long availableCopies,
                       long totalCopies,
                       long pendingFirstAccess,
                       long loansInPeriod,
                       long returnsInPeriod,
                       long exitsInPeriod,
                       BigDecimal salesThisMonth,
                       long receiptsThisMonth) {

        /** Empréstimos + devoluções + saídas no período (o backend não serializa o método dele). */
        public long movementsInPeriod() {
            return loansInPeriod + returnsInPeriod + exitsInPeriod;
        }

        public BigDecimal sales() {
            return salesThisMonth == null ? BigDecimal.ZERO : salesThisMonth;
        }
    }

    public record Diary(LocalDate day, long loans, long returns) {}

    public record CountByStatus(String status, long quantity) {}

    public record DepartmentOccupation(String department, long loaned, long total) {
        public int percent() {
            return total == 0 ? 0 : (int) Math.round(loaned * 100.0 / total);
        }
    }

    public record DelayedLoan(long loanId, String studentName, String ra, String copies,
                              LocalDate dueDate, long delayedDays) {
        /** Faixa de gravidade para a cor do selo: 3 = mais de 14 dias, 2 = 7 a 14, 1 = até 6. */
        public int severity() {
            if (delayedDays > 14) return 3;
            if (delayedDays >= 7) return 2;
            return 1;
        }
    }

    public record SecuritySummary(long loginSuccess, long loginFailure, long mfaFailure,
                                  long accessDenied, long passwordResets) {}

    public static DashboardData empty(int days) {
        return new DashboardData(days, LocalDateTime.now(),
                new Kpis(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, 0),
                List.of(), List.of(), List.of(), List.of(),
                new SecuritySummary(0, 0, 0, 0, 0), List.of());
    }
}
