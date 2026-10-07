package lidosunset.repository;

import lidosunset.model.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAttivaTrue();
    Optional<Cliente> findByIdAndAttivaTrue(Long id);
    Page<Cliente> findByAttivaTrue(Pageable pageable);

    // Cerca clienti per email
    Optional<Cliente> findByEmailAndAttivaTrue(String email);

    // Cerca clienti per cognome
    List<Cliente> findByCognomeAndAttivaTrue(String cognome);

    // Cerca clienti per nome e cognome
    List<Cliente> findByNomeAndCognomeAndAttivaTrue(String nome, String cognome);

    // Ricerca flessibile: nome O cognome contengono la query (case-insensitive),
    // usata per la ricerca "live" nel form di creazione prenotazione
    @Query("SELECT c FROM Cliente c WHERE c.attiva = true AND " +
            "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.cognome) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Cliente> cercaPerNomeOCognome(@Param("query") String query);
}