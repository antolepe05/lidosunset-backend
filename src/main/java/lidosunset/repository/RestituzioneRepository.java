package lidosunset.repository;

import lidosunset.model.entity.Manutenzione;
import lidosunset.model.entity.RestituzioneAttrezzatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestituzioneRepository extends JpaRepository<RestituzioneAttrezzatura, Long> {

    // Cerca la restituzione associata a una prenotazione specifica. Si usa Optional perché
    // una prenotazione potrebbe non avere ancora una restituzione
    Optional<RestituzioneAttrezzatura> findByPrenotazioneId(Long prenotazioneId);

    // Cerca tutte le restituzioni con danni segnalati
    List<RestituzioneAttrezzatura> findByDanniTrue();
}
