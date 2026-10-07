package lidosunset.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO per il Cliente. Usato per trasferire i dati del cliente tra il layer di persistenza e l'API REST, senza esporre direttamente l'entity JPA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    @Builder.Default
    private boolean attiva = true;
}
