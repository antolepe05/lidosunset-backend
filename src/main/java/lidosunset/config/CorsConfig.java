package lidosunset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Configurazione CORS (Cross-Origin Resource Sharing).
// Permette al frontend Angular (localhost:4200) di comunicare
// con il backend Spring Boot (localhost:8080).
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origini permesse: il frontend Angular in sviluppo
        config.addAllowedOrigin("http://localhost:4200");

        // Metodi HTTP permessi
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Headers permessi (incluso Authorization per il token JWT)
        config.addAllowedHeader("*");

        // Permette l'invio di credenziali (token JWT nell'header)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}