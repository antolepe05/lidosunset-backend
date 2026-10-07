package lidosunset.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO per la RigaAcquistoRistorante. Rappresenta un singolo prodotto all'interno di un ordine ristorante.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RigaAcquistoRistoranteDto {
    private Long id;
    private String prodotto;
    private Integer quantita;
    private Double prezzoUnitario;
    private Double prezzoTotale;
    private String note;

    @Builder.Default
    private boolean attiva=false;
}
