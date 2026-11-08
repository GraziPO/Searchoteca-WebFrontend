package searchoteca.frontend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Extrai a mensagem que o backend mandou no corpo do erro ({status, error, message, timestamp}),
 * para mostrar ao usuário no próprio formulário em vez da página de erro.
 */
public final class BackendMessages {
    private static final Logger log = LoggerFactory.getLogger(BackendMessages.class);

    private BackendMessages() {}

    public static String of(RestClientException ex, String fallback) {
        if (ex instanceof ResourceAccessException) {
            log.error("Backend inacessível: {}", ex.getMessage());
            return "Não foi possível conectar ao servidor. Verifique se o backend está rodando.";
        }
        if (ex instanceof RestClientResponseException rre) {
            log.warn("Backend respondeu HTTP {}: {}", rre.getStatusCode().value(), rre.getResponseBodyAsString());
            try {
                Map<?, ?> body = rre.getResponseBodyAs(Map.class);
                if (body != null && body.get("message") instanceof String message && !message.isBlank()) {
                    return message;
                }
            } catch (Exception ignored) {
                // corpo não é JSON: usa o texto padrão
            }
            return fallback;
        }
        log.error("Falha ao chamar o backend", ex);
        return fallback;
    }

    /**
     * Para formulários: sessão expirada (401) continua indo para o BackendErrorAdvice,
     * que manda de volta ao login. Os demais erros viram mensagem no formulário.
     */
    public static String forForm(RestClientException ex, String fallback) {
        if (ex instanceof RestClientResponseException rre && rre.getStatusCode().value() == 401) {
            throw rre;
        }
        return of(ex, fallback);
    }
}
