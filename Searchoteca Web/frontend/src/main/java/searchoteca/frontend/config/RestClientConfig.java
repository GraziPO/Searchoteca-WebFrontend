package searchoteca.frontend.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    /**
     * Cliente HTTP configurado com a base URL do backend (Searchoteca).
     * Um interceptor injeta o "Authorization: Bearer <jwt>" em toda chamada,
     * lendo o token guardado na sessao HTTP do usuario logado (se houver).
     */
    @Bean
    public RestClient backendRestClient(@Value("${backend.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    String token = currentJwt();
                    if (token != null) {
                        request.getHeaders().setBearerAuth(token);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    private String currentJwt() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpSession session = attrs.getRequest().getSession(false);
        if (session == null) {
            return null;
        }
        Object token = session.getAttribute("jwt");
        return token != null ? token.toString() : null;
    }
}
