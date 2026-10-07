package lidosunset.repository;

import lidosunset.model.entity.VenditaRistorante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VenditaRistoranteRepository extends JpaRepository<VenditaRistorante, Long> {
    // Recuperat le vendite ristorative
    List<VenditaRistorante> findByAttivaTrue();

    // Recupera le vendita di una giornata specifica
    List<VenditaRistorante> findByDataAndAttivaTrue(LocalDate data);

    // Recupera una vendita specifica solo se attiva
    Optional<VenditaRistorante> findByIdAndAttivaTrue(Long id);
}
