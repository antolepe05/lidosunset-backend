package lidosunset.service;

import lidosunset.model.dto.AuthResponse;
import lidosunset.model.dto.LoginRequest;
import lidosunset.model.dto.RegisterRequest;
import lidosunset.model.entity.Cliente;
import lidosunset.model.entity.Role;
import lidosunset.model.entity.Utente;
import lidosunset.repository.ClienteRepository;
import lidosunset.repository.UtenteRepository;
import lidosunset.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UtenteRepository utenteRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpirationMs;

    public AuthService(UtenteRepository utenteRepository, ClienteRepository clienteRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.utenteRepository = utenteRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Gestisce la registrazione. Si controllano unicità di username e email. Si costruisce l'utente con password
    // HASHATA via BCrypt, salva sul DB ed emette un token, così è subito loggato.
    // Viene creata ANCHE l'anagrafica Cliente con la stessa email, altrimenti il
    // nuovo utente potrebbe autenticarsi ma non riuscire a prenotare nulla.
    @Transactional
    public AuthResponse register(RegisterRequest request){
        if (utenteRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Username già in uso");
        }
        if (utenteRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email già in uso");
        }
        Utente utente = Utente.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENTE)
                .build();

        utenteRepository.save(utente);

        // Crea l'anagrafica cliente collegata, solo se non esiste già un cliente con quella email
        if (clienteRepository.findByEmailAndAttivaTrue(request.getEmail()).isEmpty()){
            Cliente cliente = Cliente.builder()
                    .nome(request.getNome())
                    .cognome(request.getCognome())
                    .email(request.getEmail())
                    .telefono(request.getTelefono())
                    .attiva(true)
                    .build();
            clienteRepository.save(cliente);
        }

        String token = jwtService.generateToken(claimsConRuolo(utente), utente);
        return new AuthResponse(token, utente.getUsername(), utente.getRole().name(), jwtExpirationMs);
    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Utente utente = utenteRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        String token = jwtService.generateToken(claimsConRuolo(utente), utente);
        return new AuthResponse(token, utente.getUsername(), utente.getRole().name(), jwtExpirationMs);
    }

    private Map<String, Object> claimsConRuolo(Utente utente){
        Map<String, Object> extra = new HashMap<>();
        extra.put("role", utente.getRole().name());
        return extra;
    }
}