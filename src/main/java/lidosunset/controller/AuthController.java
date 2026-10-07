package lidosunset.controller;

import lidosunset.model.dto.LoginRequest;
import lidosunset.model.dto.RegisterRequest;
import lidosunset.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.AuthenticationException;
import java.util.Map;

// Gestisce registrazione, login e creazione cliente-con-accesso da parte
// dell'ADMIN.

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    // Auto-registrazione: chiunque può crearsi un account cliente
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try{
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // L'ADMIN crea un account di accesso per un cliente (es. al banco, di
    // persona). Riusa la stessa logica della registrazione self-service:
    // crea sia l'Utente che l'anagrafica Cliente collegata.
    @PostMapping("/registra-cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registraCliente(@RequestBody RegisterRequest request){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // Login: verifica le credenziali e restituisce il token JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            return  ResponseEntity.ok(authService.login(request));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenziali non valide"));
        }
    }
}