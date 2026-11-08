package searchoteca.frontend.dto.archive;

/** Corpo de POST /api/exemplares: cria "quantity" exemplares do ISBN na localização. */
public record CopyRequest(String isbn, String departCode, String localCode, int quantity) {}
