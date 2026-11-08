package searchoteca.frontend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import searchoteca.frontend.dto.catalog.BookDetail;
import searchoteca.frontend.dto.catalog.BookSearchResult;
import searchoteca.frontend.dto.catalog.PageResponse;

/** Consulta pública ao acervo: rotas /api/public/** não exigem token. */
@Service
public class CatalogService {

    public static final int PAGE_SIZE = 20;

    private final RestClient restClient;

    public CatalogService(RestClient backendRestClient) {
        this.restClient = backendRestClient;
    }

    public PageResponse<BookSearchResult> search(String q, int page) {
        return restClient.get()
                .uri(uri -> uri.path("/api/public/acervo")
                        .queryParam("q", q == null ? "" : q.trim())
                        .queryParam("page", Math.max(page, 0))
                        .queryParam("size", PAGE_SIZE)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<BookSearchResult>>() {});
    }

    public BookDetail detail(String isbn) {
        return restClient.get()
                .uri("/api/public/acervo/{isbn}", isbn)
                .retrieve()
                .body(BookDetail.class);
    }
}
