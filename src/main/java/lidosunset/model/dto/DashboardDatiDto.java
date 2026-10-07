package lidosunset.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DashboardDatiDto {
    private BigDecimal incassoTotalePrenotazioni;
    private BigDecimal incassoTotaleRistorante;
    private BigDecimal totaleSpeseRistorante;
    private BigDecimal totaleSpeseAttrezzature;
    private BigDecimal totaleSpeseManutenzione;

    private BigDecimal margineNetto;

    private Long totaleAttrezzatura;
    private Map<String, Long> contatoriPerStato;

    private Long manutenzioniUrgentiAttive;
}