package searchoteca.frontend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.model.Book;
import searchoteca.frontend.model.Department;
import searchoteca.frontend.model.Location;

import java.util.List;

@Service
public class BackendService {

    private final RestClient restClient;

    public BackendService(RestClient backendRestClient) {
        this.restClient = backendRestClient;
    }

    // ---------------- LIVROS ----------------

    public List<Book> listBooks() {
        return restClient.get()
                .uri("/api/livro")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>() {});
    }

    public Book getBook(String isbn) {
        return restClient.get()
                .uri("/api/livro/{isbn}", isbn)
                .retrieve()
                .body(Book.class);
    }

    public Book createBook(Book book) {
        return restClient.post()
                .uri("/api/livro")
                .body(book)
                .retrieve()
                .body(Book.class);
    }

    public Book updateBook(String isbn, Book book) {
        return restClient.put()
                .uri("/api/livro/{isbn}", isbn)
                .body(book)
                .retrieve()
                .body(Book.class);
    }

    public void deleteBook(String isbn) {
        restClient.delete()
                .uri("/api/livro/{isbn}", isbn)
                .retrieve()
                .toBodilessEntity();
    }

    // ---------------- DEPARTAMENTOS ----------------

    public List<Department> listDepart() {
        return restClient.get()
                .uri("/api/departamento")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Department>>() {});
    }

    public Department getDepart(String departCode) {
        return restClient.get()
                .uri("/api/departamento/{departCode}", departCode)
                .retrieve()
                .body(Department.class);
    }

    public Department createDepart(Department depart) {
        return restClient.post()
                .uri("/api/departamento")
                .body(depart)
                .retrieve()
                .body(Department.class);
    }

    public Department updateDepart(String departCode, Department depart) {
        return restClient.put()
                .uri("/api/departamento/{departCode}", departCode)
                .body(depart)
                .retrieve()
                .body(Department.class);
    }

    public void deleteDepart(String departCode) {
        restClient.delete()
                .uri("/api/departamento/{departCode}", departCode)
                .retrieve()
                .toBodilessEntity();
    }

    // ---------------- LOCALIZACOES ----------------

    public List<Location> listLocal() {
        return restClient.get()
                .uri("/api/localizacao")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Location>>() {});
    }

    public Location getLocal(String localCode) {
        return restClient.get()
                .uri("/api/localizacao/{localCode}", localCode)
                .retrieve()
                .body(Location.class);
    }

    public Location createLocal(Location local) {
        return restClient.post()
                .uri("/api/localizacao")
                .body(local)
                .retrieve()
                .body(Location.class);
    }

    public Location updateLocal(String localCode, Location local) {
        return restClient.put()
                .uri("/api/localizacao/{localCode}", localCode)
                .body(local)
                .retrieve()
                .body(Location.class);
    }

    public void deleteLocal(String localCode) {
        restClient.delete()
                .uri("/api/localizacao/{localCode}", localCode)
                .retrieve()
                .toBodilessEntity();
    }
}
