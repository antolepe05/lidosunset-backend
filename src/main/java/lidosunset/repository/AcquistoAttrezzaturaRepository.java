package lidosunset.repository;

import lidosunset.model.entity.AcquistoAttrezzatura;
import lidosunset.model.entity.StatoAcquisto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface AcquistoAttrezzaturaRepository extends JpaRepository<AcquistoAttrezzatura, Long> {

    List<AcquistoAttrezzatura> findByAttivaTrue();
    Optional<AcquistoAttrezzatura> findByIdAndAttivaTrue(Long id);

    // Cerca tutti gli acquisti per stato
    List<AcquistoAttrezzatura> findByStato (StatoAcquisto stato);

    // Cerca tutti gli gli acquisti di un fornitore specifico
    List<AcquistoAttrezzatura> findByFornitoreId (Long fornitoreId);

    // Cerca tutti gli acquisti di un fornitore per stato
    List<AcquistoAttrezzatura> findByFornitoreIdAndStato (Long fornitoreId, StatoAcquisto stato);

    List<AcquistoAttrezzatura> findByFornitoreIdAndAttivaTrue(Long fornitoreId);
}
