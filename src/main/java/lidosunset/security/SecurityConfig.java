package lidosunset.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Configurazione centrale di Spring Security.

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    // @Lazy su UserDetailsService evita una dipendenza circolare: questa
    // classe definisce PasswordEncoder e usa userDetailsService per costruire
    // l'AuthenticationProvider, mentre CustomUserDetailsService (che la
    // implementa) dipende a sua volta da altri bean - costruendo tutto in
    // modo "lazy", Spring rimanda la creazione effettiva del bean al primo
    // utilizzo reale, invece di doverlo risolvere subito durante l'avvio.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, @Lazy UserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    // Bean per l'hashing delle password (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Collega UserDetailsService e PasswordEncoder: è il componente che
    // Spring Security usa per verificare username+password durante il login
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    // Espone l'AuthenticationManager standard di Spring
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception{
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Attiva il CORS usando il bean definito sotto
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disabilita CSRF non necessario con JWT stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Regole di autorizzazione per URL
                .authorizeHttpRequests(auth -> auth
                        // Login e registrazione sono pubblici
                        .requestMatchers("/api/auth/**").permitAll()
                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                )

                // Gestisce le richieste non autenticate restituendo 401 invece del 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))

                // Sessione STATELESS
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                // Inserisce il filtro JWT prima di UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Consenti al frontend Angular di dialogare con il backend
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        // Consenti tutti i metodi HTTP necessari, incluso OPTIONS per il preflight
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Consenti gli header necessari per far passare il JWT
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        // Permetti l'invio di credenziali/cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}