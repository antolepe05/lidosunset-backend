package lidosunset.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

// DTO per la Restituzione. Usato per trasferire i dati della prenotazione tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestituzioneDto {
    private Long id;
    private LocalDate data;
    private LocalTime ora;
    private boolean danni;
    private String descrizioneDanni;

    // ID della prenotazione associata alla restituzione
    private Long prenotazioneId;
}
