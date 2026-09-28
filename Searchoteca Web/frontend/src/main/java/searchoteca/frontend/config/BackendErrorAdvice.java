package searchoteca.frontend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Map;

/**
 * Antes: qualquer erro do backend (403, 404, 409...) virava "Whitelabel Error Page 500".
 * Agora: mostra o status real e a mensagem que o backend mandou; se a sessão
 * expirou (401), volta para o login.
 */
@ControllerAdvice
public class BackendErrorAdvice {

    private static final Logger log = LoggerFactory.getLogger(BackendErrorAdvice.class);

    @ExceptionHandler(RestClientResponseException.class)
    public ModelAndView handleBackendError(RestClientResponseException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        log.warn("{} {} -> backend respondeu HTTP {}: {}",
                request.getMethod(), request.getRequestURI(), status, ex.getResponseBodyAsString());

        if (status == 401) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            RequestContextUtils.getOutputFlashMap(request)
                    .put("erro", "Sua sessão expirou. Entre novamente.");
            return new ModelAndView("redirect:/login");
        }

        String message = backendMessage(ex);
        return errorPage(status, message != null ? message : defaultMessage(status));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ModelAndView handleBackendOffline(ResourceAccessException ex, HttpServletRequest request) {
        log.error("{} {} -> backend inacessível: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return errorPage(503, "Não foi possível conectar ao servidor. Verifique se o backend está rodando.");
    }

    private ModelAndView errorPage(int status, String message) {
        ModelAndView mv = new ModelAndView("erro");
        mv.setStatus(HttpStatusCode.valueOf(status));
        mv.addObject("status", status);
        mv.addObject("mensagem", message);
        return mv;
    }

    private String backendMessage(RestClientResponseException ex) {
        try {
            Map<?, ?> body = ex.getResponseBodyAs(Map.class);
            if (body != null && body.get("message") instanceof String message && !message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
            // corpo não é JSON
        }
        return null;
    }

    private String defaultMessage(int status) {
        return switch (status) {
            case 403 -> "Você não tem permissão para esta ação.";
            case 404 -> "Registro não encontrado.";
            case 409 -> "Operação não permitida no estado atual dos dados.";
            case 400 -> "Dados inválidos. Confira o formulário.";
            default -> "Ocorreu um erro no servidor. Tente novamente.";
        };
    }
}
