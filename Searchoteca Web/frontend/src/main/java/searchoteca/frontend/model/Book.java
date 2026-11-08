package searchoteca.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Espelha searchoteca.model.Books.BookModel no backend.
 * Departamento e localização ficam nos exemplares (org_copies), não no livro.
 * O preço de venda é só leitura aqui: o cadastro e a edição do livro não o alteram
 * (o backend zera o preço no cadastro); ele muda pela rota própria /preco.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Integer rel_year;
    private String publisher;
    private String genre;
    private BigDecimal salePrice;

    public Book() {}
}
