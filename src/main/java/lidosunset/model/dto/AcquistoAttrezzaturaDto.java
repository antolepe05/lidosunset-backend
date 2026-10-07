package lidosunset.model.dto;

import lidosunset.model.entity.StatoAcquisto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// DTO per la AcquistoAttrezzatura. Usato per trasferire i dati della prenotazione tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquistoAttrezzaturaDto {
    private Long id;
    private LocalDate dataOrdine;
    private LocalDate dataRicezione;
    private Double prezzoTotale;
    private StatoAcquisto stato;
    private String note;
    @Builder.Default
    private boolean attiva=true;

    // ID, nome del fornitore associato all'ordine
    private Long fornitoreId;

    // Lista delle righe dell'ordine (prodotti ordinati)
    private List<RigaAcquistoAttrezzaturaDto> righe;
}
