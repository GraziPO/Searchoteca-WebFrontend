package searchoteca.frontend.dto.circulation;

import java.util.List;

/** Corpo de POST /api/saidas. type: SALE, DONATION ou WRITE_OFF. */
public record ExitRequest(String type, List<Long> copyIds, String notes) {}
