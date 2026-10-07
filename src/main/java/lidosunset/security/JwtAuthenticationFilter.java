package lidosunset.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// Filtro JWT che intercetta ogni richiesta HTTP una sola volta.
// Se trova un token JWT valido nell'header Authorization, popola il SecurityContext con l'utente autenticato.
// Si inserisce nella filter chain PRIMA di UsernamePasswordAuthenticationFilter, così quando Spring arriva al filtro
// "standard" trova gia' un'autenticazione nel context e lo lascia passare.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException{
        final String authHeader = request.getHeader("Authorization");

        // Se l'header manca o non è nel formato "Bearer <token>", passa al filtro successivo senza autenticare
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Rimuove il prefisso "Bearer "
        final String jwt = authHeader.substring(7);

        try{
            final String username = jwtService.extractUsername(jwt);

            // Procede solo se lo username è presente e l'utente non è già autenticato nel SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)){
                    // Crea il token di autorizzazione con 3 argomenti: principal, credentials e authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException ex){
            // Token malformato, scaduto o firma errata. Non si popola il SecurityContext:
            // Spring security risponderà 401 se l'endpoint richiede autenticazione
        }
        filterChain.doFilter(request,response);
    }
}
