package lidosunset.repository;

import lidosunset.model.entity.Prenotazione;
import lidosunset.model.entity.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// @Repository indica a Spring che l'interfaccia è un componente del layer di accesso ai dati.
// Estendendo JpaRepository si ottengono automaticamente i metodi CRUD
@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {

    List<Prenotazione> findByAttivaTrue();
    Optional<Prenotazione> findByIdAndAttivaTrue(Long id);

    // Cerca tutte le prenotazioni di un cliente
    List<Prenotazione> findByClienteIdAndAttivaTrue(Long clienteId);

    // Cerca tutte le prenotazioni di un'attrezzatura
    List<Prenotazione> findByAttrezzaturaIdAndAttivaTrue(Long attrezzaturaId);

    // Cerca tutte le prenotazioni per stato
    List<Prenotazione> findByStatoAndAttivaTrue(StatoPrenotazione stato);

    // Cerca tutte le prenotazioni in una data specifica
    List<Prenotazione> findByDataAndAttivaTrue(LocalDate data);

    // Cerca le prenotazioni di un cliente per stato
    List<Prenotazione> findByClienteIdAndStatoAndAttivaTrue(Long clienteId, StatoPrenotazione stato);

    // Cerca le prenotazioni di un'attrezzatura in una data specifica
    List<Prenotazione> findByAttrezzaturaIdAndDataAndAttivaTrue(Long attrezzaturaId, LocalDate data);

    @Query("SELECT COUNT(p) > 0 FROM Prenotazione p WHERE p.attrezzatura.id = :attrezzaturaId " +
            "AND p.data = :data "+
            "AND p.attiva = true "+
            "AND p.oraInizio < :oraFine "+
            "AND p.oraFine > :oraInizio "+
            "AND (:id IS NULL OR p.id != :id)")
    boolean isAttrezzaturaOccupata(
            @Param("attrezzaturaId") Long attrezzaturaId,
            @Param("data") LocalDate data,
            @Param("oraInizio")LocalTime oraInizio,
            @Param("oraFine") LocalTime oraFine,
            @Param("id") Long id
    );

}
