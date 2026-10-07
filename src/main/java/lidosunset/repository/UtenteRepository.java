package lidosunset.repository;

import lidosunset.model.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    // Cerca un utente per username.
    Optional<Utente> findByUsername(String username);

    // Cerca un utente oer username oppure per email
    Optional<Utente> findByUsernameOrEmail(String username, String email);

    // Verifica se esiste già un utente con questo username.Viene usato in fase di registrazione per evitare duplicati.
    boolean existsByUsername(String username);

    // Verifica se esiste già un utente con questa email. Viene usato in fase di registrazione per evitare duplicati.
    boolean existsByEmail(String email);
}
