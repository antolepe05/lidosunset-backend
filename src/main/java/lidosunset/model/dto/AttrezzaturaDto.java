package lidosunset.model.dto;

import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.TipoAttrezzatura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// DTO per l'Attrezzatura. Usato per trasferire i dati dell'attrezzatura tra il layer di persistenza e l'API REST,
// senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttrezzaturaDto {
    private Long id;
    private String nome;
    private TipoAttrezzatura tipo;
    private StatoAttrezzatura stato;
    private String descrizione;
    @Builder.Default
    private boolean attiva=true;
}
