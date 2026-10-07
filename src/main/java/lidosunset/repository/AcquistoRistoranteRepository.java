package lidosunset.repository;

import lidosunset.model.entity.AcquistoRistorante;
import lidosunset.model.entity.StatoAcquisto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface AcquistoRistoranteRepository extends JpaRepository<AcquistoRistorante, Long> {

    List<AcquistoRistorante> findByAttivaTrue();
    Optional<AcquistoRistorante> findByIdAndAttivaTrue(Long id);


    // Cerca tutti gli acquisti per stato
    List<AcquistoRistorante> findByStatoAndAttivaTrue(StatoAcquisto stato);

    // Cerca tutti gli acquisti di un fornitore specifico
    List<AcquistoRistorante> findByFornitoreIdAndAttivaTrue(Long fornitoreId);

    //Cerca tutti gli acquisti di un fornitore per stato
    List<AcquistoRistorante> findByFornitoreIdAndStatoAndAttivaTrue(Long fornitoreId, StatoAcquisto stato);

}
