package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="riga_acquisto_attrezzatura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RigaAcquistoAttrezzatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo di attrezzatura ordinata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAttrezzatura tipoAttrezzatura;

    // Quantita ordinata per tipo di attrezzatura
    @Column(nullable = false)
    private Integer quantita;

    // Prezzo unitario per tipo di attrezzatura
    @Column(nullable = false)
    private Double prezzoUnitario;

    //Prezzo totale della riga (quantita*prezzoUnitario)
    @Column(nullable = false)
    private Double prezzoTotale;

    // Note aggiuntive
    @Column
    private String note;

    // Molte righe appartengono a un solo ordine acquisto attrezzatura
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acquisto_attrezzatura_id", nullable = false)
    private AcquistoAttrezzatura acquistoAttrezzatura;

}
