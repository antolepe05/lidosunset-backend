package lidosunset.repository;

import lidosunset.model.entity.Manutenzione;
import lidosunset.model.entity.TipoManutenzione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface ManutenzioneRepository extends JpaRepository<Manutenzione, Long> {

    List<Manutenzione> findByAttivaTrue();
    Optional<Manutenzione> findByIdAndAttivaTrue(Long id);

    // Cerca tutte le manutenzioni attive di un'attrezzatura
    List<Manutenzione> findByAttrezzaturaIdAndAttivaTrue(Long id);

    // Cerca tutte le manutenzioni per tipo
    List<Manutenzione> findByTipoAndAttivaTrue(TipoManutenzione tipo);

    // Cerca le manutenzioni non ancora completate e attive
    List<Manutenzione> findByCompletataFalseAndAttivaTrue();

    // Cerca le manutenzioni non completate di un'attrezzatura specifica e ancora attiva
    List<Manutenzione> findByAttrezzaturaIdAndCompletataFalseAndAttivaTrue(Long attrezzaturaId);


}
