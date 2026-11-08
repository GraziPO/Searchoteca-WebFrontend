package searchoteca.frontend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.dto.AuditLog;
import searchoteca.frontend.dto.archive.BatchMoveRequest;
import searchoteca.frontend.dto.archive.BatchMoveResult;
import searchoteca.frontend.dto.archive.CopyLabel;
import searchoteca.frontend.dto.archive.CopyRequest;
import searchoteca.frontend.dto.archive.PriceRequest;
import searchoteca.frontend.dto.auth.ChangePasswordRequest;
import searchoteca.frontend.dto.dashboard.DashboardData;
import searchoteca.frontend.dto.users.StudentRequest;
import searchoteca.frontend.model.Book;
import searchoteca.frontend.model.Department;
import searchoteca.frontend.model.Location;
import searchoteca.frontend.model.User;

import java.math.BigDecimal;
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

    /** Só o root tem org_books:edit_price; null remove o preço. */
    public Book updateBookPrice(String isbn, BigDecimal salePrice) {
        return restClient.post()
                .uri("/api/livro/{isbn}/preco", isbn)
                .body(new PriceRequest(salePrice))
                .retrieve()
                .body(Book.class);
    }

    // ---------------- EXEMPLARES ----------------

    /** Cria os exemplares e devolve as etiquetas já com o código gerado pelo banco. */
    public List<CopyLabel> createCopies(CopyRequest request) {
        return restClient.post()
                .uri("/api/exemplares")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CopyLabel>>() {});
    }

    /** Reimpressão: o parâmetro do backend se chama "labels". */
    public List<CopyLabel> findLabels(List<Long> copyIds) {
        return restClient.get()
                .uri(uri -> uri.path("/api/exemplares/etiquetas")
                        .queryParam("labels", copyIds.toArray())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CopyLabel>>() {});
    }

    public BatchMoveResult moveCopies(BatchMoveRequest request) {
        return restClient.post()
                .uri("/api/exemplares/movimentacao")
                .body(request)
                .retrieve()
                .body(BatchMoveResult.class);
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

    // ---------------- USUARIOS ----------------
    // Obs: o backend nao expoe DELETE para usuarios, so ativar/desativar.

    public List<User> listUsers() {
        return restClient.get()
                .uri("/api/usuarios")
                .retrieve()
                .body(new ParameterizedTypeReference<List<User>>() {});
    }

    public User getUser(String username) {
        return restClient.get()
                .uri("/api/usuarios/{username}", username)
                .retrieve()
                .body(User.class);
    }

    public User createUser(User user) {
        return restClient.post()
                .uri("/api/usuarios")
                .body(user)
                .retrieve()
                .body(User.class);
    }

    public User updateUser(String username, User user) {
        return restClient.put()
                .uri("/api/usuarios/{username}", username)
                .body(user)
                .retrieve()
                .body(User.class);
    }

    public void activateUser(String username) {
        restClient.patch()
                .uri("/api/usuarios/{username}/status/ativar", username)
                .retrieve()
                .toBodilessEntity();
    }

    public void deactivateUser(String username) {
        restClient.patch()
                .uri("/api/usuarios/{username}/status/desativar", username)
                .retrieve()
                .toBodilessEntity();
    }

    /** O backend responde 200 sem corpo. */
    public void createStudent(StudentRequest request) {
        restClient.post()
                .uri("/api/usuarios/aluno")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /** Troca voluntária: o id do usuário vem do token, não da URL. */
    public void changeOwnPassword(String currentPassword, String newPassword) {
        restClient.post()
                .uri("/api/usuarios/me/senha")
                .body(new ChangePasswordRequest(currentPassword, newPassword))
                .retrieve()
                .toBodilessEntity();
    }

    // ---------------- AUDITORIA ----------------

    /** O backend limita entre 1 e 500 registros. */
    public List<AuditLog> listAudit(String username, int limit) {
        return restClient.get()
                .uri(uri -> {
                    uri.path("/api/auditTrail").queryParam("limite", limit);
                    if (username != null && !username.isBlank()) {
                        uri.queryParam("usuario", username.trim());
                    }
                    return uri.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<AuditLog>>() {});
    }

    // ---------------- DASHBOARD ----------------

    /** GET /api/dashboard?dias=N — exige dashboard:view (403 para quem não tem). */
    public DashboardData dashboard(int days) {
        return restClient.get()
                .uri(uri -> uri.path("/api/dashboard").queryParam("dias", days).build())
                .retrieve()
                .body(DashboardData.class);
    }
}
