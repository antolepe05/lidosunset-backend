package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name="acquisto_ristorante")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquistoRistorante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data in cui è stato effettuato l'acquisto
    @Column(nullable = false)
    private LocalDate dataOrdine;

    // Data in cui è prevista la consegna o in cui è stato ricevuto l'ordine
    @Column
    private LocalDate dataRicezione;

    // Prezzo totale dell'ordine
    @Column(nullable = false)
    private BigDecimal prezzo;

    // Stato dell ordine dove i valori sono contenuti in StatoAcquisto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoAcquisto stato;

    // Note aggiuntive sull'ordine
    @Column
    private String note;


    @Column(nullable = false)
    @Builder.Default
    private boolean attiva=true;

    // Molti acquisti possono essere fatti allo stesso fornitore. Il fornitore viene caricato
    // solo quando esplicitamente richiesto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="fornitore_id", nullable=false)
    private Fornitore fornitore;

    // Un acquisto ristorante è composto da molte righe d'ordine, ciascuna corrispondente a un prodotto diverso.
    // La relazione è gestita dal campo "acquistoRistorante" in RigaAcquistoRistorante. Le righe vengono
    // salvate/eliminate insieme all'ordine.
    @OneToMany(mappedBy = "acquistoRistorante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RigaAcquistoRistorante> righe;
}
