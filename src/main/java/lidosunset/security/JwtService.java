package lidosunset.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
* SERVIZIO che si occupa di TUTTO cio' che riguarda i token JWT:
*   - generarne uno alla login
*   - estrarne lo username
*   - validarlo (firma valida + non scaduto + appartiene a questo utente)
* E' una classe pura, senza stato: l'unica "memoria" e' la chiave segreta,
* letta da application.properties. Cosi' restando @Service Spring la istanzia
* come singleton e la inietta dove serve (filtro JWT + AuthService).
*/
@Service
public class JwtService {
    /*
     * Chiave segreta usata per firmare e verificare i token.
     * Iniettata da application.properties (proprieta' "application.security.jwt.secret-key").
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Durata del token in millisecondi
    @Value("${application.security.jwt.expiration}")
    private long jwtExpirationMs;

    // Estrae lo username (claim "sub") dal token.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Estrae un claim generico.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parsa il token e verifica la firma. Lancia eccezione se invalido o scaduto.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Genera un token con solo lo username come subject.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Permette di aggiungere "extra claims" personalizzati al payload (es. il ruolo, l'id utente, ecc.). Sono comodi lato client
    // perchè permettono di decidere cosa mostrare nella UI senza chiamare di nuovo il backend.
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    // Il token è valido se la firma è corretta, lo username coincide con quello dell'utente caricato dal DB e non è scaduto.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Trasforma la stringa Base64 in una SecretKey per HS256.
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
