package lidosunset.security;

import lidosunset.repository.UtenteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


/*
 * Implementazione dell'interfaccia UserDetailsService di Spring Security.
 *
 * Spring Security non sa nulla del nostro DB: ha bisogno di un "ponte" che,
 * dato uno username, gli restituisca un UserDetails. Questa classe e' quel
 * ponte. Verra' usata:
 *   - dall'AuthenticationManager durante il login (per recuperare l'utente
 *     e confrontare la password fornita con l'hash sul DB)
 *   - dal JwtAuthenticationFilter ad ogni richiesta protetta (per ricaricare
 *     l'utente dal DB e verificare che il token sia ancora valido)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    public CustomUserDetailsService(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException{
        // findByUsernameOrEmail permette di fare login indifferentemente con
        // username o email - ma il risultante UserDetails (sotto) usa sempre
        // utente.getUsername(), MAI l'email.
        return utenteRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(utente -> new User(
                        utente.getUsername(),
                        utente.getPassword(),
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_"+utente.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username o email: "+usernameOrEmail));
    }
}
