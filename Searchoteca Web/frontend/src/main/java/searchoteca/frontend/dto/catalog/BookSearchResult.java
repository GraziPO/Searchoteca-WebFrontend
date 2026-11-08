package searchoteca.frontend.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookSearchResult(String isbn, String title, String author, String publisher,
                               String genre, Integer relYear, int totalCopies, int availableCopies) {}
