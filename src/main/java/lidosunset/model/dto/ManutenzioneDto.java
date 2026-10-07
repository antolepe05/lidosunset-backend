package lidosunset.model.dto;

import lidosunset.model.entity.TipoManutenzione;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO per la Manutenzione. Usato per trasferire i dati della prenotazione tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManutenzioneDto {
    private Long id;
    private LocalDate data;
    private String descrizione;
    private TipoManutenzione tipo;
    private boolean completata;
    private String note;
    @Builder.Default
    private boolean attiva=true;
    private BigDecimal costo;

    // ID e nome dell'attrezzatura associata alla manutenzione.
    private Long attrezzaturaId;
    private String attrezzaturaNome;
}
