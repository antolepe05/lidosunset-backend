package lidosunset.model.dto;

import lidosunset.model.entity.StatoAcquisto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// DTO per l'AcquistoRistorante. Contiene le informazioni generali dell'ordine e la lista delle righe prodotti.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquistoRistoranteDto {
    private Long id;
    private LocalDate dataOrdine;
    private LocalDate dataRicezione;
    private BigDecimal prezzoTotale;
    private StatoAcquisto stato;
    private String note;
    @Builder.Default
    private boolean attiva=true;

    // ID e nome del fornitore associto all'ordine
    private Long fornitoreId;
    private String fornitoreNome;

    // Lista delle righe dell'ordine (prodotti ordinati)
    private List<RigaAcquistoRistoranteDto> righe;

}
