package searchoteca.frontend.dto.archive;

import java.math.BigDecimal;

/** Corpo de POST /api/livro/{isbn}/preco. null remove o preço. */
public record PriceRequest(BigDecimal salePrice) {}
