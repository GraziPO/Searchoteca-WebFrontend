package searchoteca.frontend.dto.archive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Dados de uma etiqueta, devolvidos pelo cadastro, pela reimpressão e pela movimentação. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CopyLabel(Long copyId, String code, String title, String author,
                        String departCode, String localCode) {}
