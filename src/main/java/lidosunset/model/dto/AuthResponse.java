package lidosunset.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO di risposta degli endpoint di auth (register / login).
 *
 * Restituisce il token JWT e qualche informazione utile al client per la UI
 * (username, ruolo). Il client memorizza il token (tipicamente in memoria o
 * in localStorage) e lo invia in ogni richiesta successiva nell'header
 * Authorization: Bearer <token>.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String ruolo;
    private long expiresInMs;
}
