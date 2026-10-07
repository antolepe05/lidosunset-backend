package lidosunset.repository;

import lidosunset.model.entity.Attrezzatura;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.TipoAttrezzatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD

@Repository
public interface AttrezzaturaRepository extends JpaRepository<Attrezzatura, Long> {

    List<Attrezzatura> findByAttivaTrue();
    Optional<Attrezzatura> findByIdAndAttivaTrue(Long id);

    // Cerca tutte le attrezzature per stato
    List<Attrezzatura> findByStatoAndAttivaTrue(StatoAttrezzatura stato);

    // Cerca tutte le attrezzature per titpo
    List<Attrezzatura> findByTipoAndAttivaTrue(TipoAttrezzatura tipo);

    // Cerca tutte le attrezzature per tipo e stato
    List<Attrezzatura> findByTipoAndStatoAndAttivaTrue(TipoAttrezzatura tipo, StatoAttrezzatura stato);
}
