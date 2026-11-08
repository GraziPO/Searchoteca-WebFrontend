package searchoteca.frontend.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** No backend o record se chama CopyAvaliability; o nome da classe não importa para o JSON. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CopyAvailability(String departCode, String departName, String localCode,
                               String localName, int available, int total) {}
