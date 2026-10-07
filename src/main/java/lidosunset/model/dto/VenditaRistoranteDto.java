package lidosunset.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenditaRistoranteDto {
    private Long id;
    private LocalDate data;
    private BigDecimal totale;
    private String note;

    @Builder.Default
    private boolean attiva = true;
}
