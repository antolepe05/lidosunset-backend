package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="riga_acquisto_ristorante")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RigaAcquistoRistorante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome del prodotto ordinato
    @Column(nullable = false)
    private String prodotto;

    // Quantità del prodotto ordinato
    @Column(nullable = false)
    private Integer quantita;

    // Prezzo unitario del prodotto
    @Column(nullable = false)
    private Double prezzoUnitario;

    // Prezzo totale della riga (quantita*prezzoUnitario)
    @Column(nullable = false)
    private Double prezzoTotale;

    // Note aggiuntive sul prodotto
    @Column
    private String note;

    // Molte righe appartengono ad un solo ordine di acquisto del ristorante. Inoltre,
    // l'ordine viene caricato solo quando serve
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acquisto_ristorante_id", nullable = false)
    private AcquistoRistorante acquistoRistorante;
}
