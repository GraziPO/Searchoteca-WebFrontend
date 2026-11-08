package searchoteca.frontend.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookDetail(String isbn, String title, String author, String publisher,
                         String genre, Integer relYear, List<CopyAvailability> locations) {

    public int totalAvailable() {
        return locations == null ? 0 : locations.stream().mapToInt(CopyAvailability::available).sum();
    }
}
