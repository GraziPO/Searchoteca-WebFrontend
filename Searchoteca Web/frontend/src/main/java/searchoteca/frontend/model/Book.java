package searchoteca.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book {
    private Long id;

    private String isbn;
    private String title;
    private String author;
    private int rel_year;
    private String publisher;
    private String genre;
    private String departCode;
    private String localCode;

    public Book() {}
}
