package lidosunset.model.dto;

import lidosunset.model.entity.TipoAttrezzatura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO per la RigaAcquistoAttrezzatura. Rappresenta un singolo prodotto all'interno di un ordine di attrezzature.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RigaAcquistoAttrezzaturaDto {
    private Long id;
    private TipoAttrezzatura tipoAttrezzatura;
    private Integer quantita;
    private Double prezzoUnitario;
    private Double prezzoTotale;
    private String note;
}