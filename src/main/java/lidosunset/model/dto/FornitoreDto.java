package lidosunset.model.dto;

import lidosunset.model.entity.SettoreFornitore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO per la Prenotazione. Usato per trasferire i dati della prenotazione tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FornitoreDto {
    private Long id;
    private String nome;
    private String partitaIva;
    private String email;
    private String telefono;
    private String indirizzo;
    private SettoreFornitore settore;
    @Builder.Default
    private boolean attiva=true;
}
