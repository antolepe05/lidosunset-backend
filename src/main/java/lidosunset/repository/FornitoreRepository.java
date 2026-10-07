package lidosunset.repository;

import lidosunset.model.entity.Fornitore;
import lidosunset.model.entity.SettoreFornitore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface FornitoreRepository  extends JpaRepository<Fornitore, Long> {

    List<Fornitore> findByAttivaTrue();
    Optional<Fornitore> findByIdAndAttivaTrue(Long id);

    // Cerca un fornitore per partita IVA. Si usa Optional perché potrebbe non esistere
    Optional<Fornitore> findByPartitaIva(String partitaIva);

    // Cerca un fornitore per email
    Optional<Fornitore> findByEmail(String email);

    // Cerca tutti i fornitori per settore
    List<Fornitore> findBySettore(SettoreFornitore settore);
}
