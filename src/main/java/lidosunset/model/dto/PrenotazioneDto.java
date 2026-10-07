package lidosunset.model.dto;

import jakarta.validation.constraints.*;
import lidosunset.model.entity.StatoPrenotazione;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// DTO per la Prenotazione. Usato per trasferire i dati della prenotazione tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrenotazioneDto {
    private Long id;

    @NotNull(message = "La data della prenotazione è obbligatoria.")
    private LocalDate dataInizio;

    @NotNull(message = "L'orario di inizio è obbligatorio.")
    private LocalTime oraInizio;

    @NotNull(message = "L'orario di inizio è obbligatorio.")
    private LocalTime oraFine;

    private StatoPrenotazione stato;

    @Builder.Default
    private boolean attiva = true;

    private BigDecimal prezzo;

    // ID, nome e cognome del cliente associato alla prenotazione.
    private Long clienteId;
    private String nome;
    private String cognome;

    // ID e nome dell'attrezzatura prenotata.
    @NotNull(message = "Devi selezionare un'attrezzatura per completare la prenotazione.")
    private Long attrezzaturaId;
    private String attrezzaturaNome;

    @AssertTrue(message = "L'orario di fine deve essere successivo all'orario di inizio.")
    public boolean isOrarioValido() {
        if (oraInizio == null || oraFine == null) {
            return true; // Se uno dei due è nullo, fallirà il controllo @NotNull sopra
        }
        return oraFine.isAfter(oraInizio);
    }

    @AssertTrue(message = "La durata della prenotazione deve essere un multiplo esatto di 10 minuti (es. 20, 30, 60 min).")
    public boolean isDurataMultiploDiDieci() {
        if (oraInizio == null || oraFine == null) {
            return true; // Se sono null, se ne occupa l'annotazione @NotNull
        }
        long minutiTotali = java.time.Duration.between(oraInizio, oraFine).toMinutes();
        // Verifica che i minuti siano maggiori di zero e divisibili esattamente per 10
        return minutiTotali > 0 && minutiTotali % 10 == 0;
    }
}
