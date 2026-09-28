package searchoteca.frontend.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Espelha searchoteca.model.BookModel no backend.
 * Nao tem mais localCode/departCode - isso agora vive em CopyModel (org_copies),
 * que ainda nao tem um Controller REST exposto pelo backend.
 */
@Getter
@Setter
public class Book {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Integer rel_year;
    private String publisher;
    private String genre;

    public Book() {}
}
