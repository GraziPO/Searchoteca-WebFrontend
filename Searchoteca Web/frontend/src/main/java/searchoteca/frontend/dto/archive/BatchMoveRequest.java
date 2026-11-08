package searchoteca.frontend.dto.archive;

import java.util.List;

/** Corpo de POST /api/exemplares/movimentacao. */
public record BatchMoveRequest(String localCode, List<String> copyCodes, Integer expectedQuantity) {}
