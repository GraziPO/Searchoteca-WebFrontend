package searchoteca.frontend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginRequiredInterceptor())
                .excludePathPatterns(
                        "/", "/login", "/mfa", "/mfa/verify", "/mfa/resend", "/logout",
                        "/termos", "/termos/aceitar", "/termos-de-uso", "/politica-de-privacidade",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico"
                );
    }
}
