package lidosunset.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name="acquisto_attrezzatura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquistoAttrezzatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data in cui è stato effettuato l'acquisto
    @Column(nullable = false)
    private LocalDate dataOrdine;

    // Data in cui è prevista la consegna o in cui è stato ricevuto l'ordine
    @FutureOrPresent(message = "La ricezione non può essere in una data passata")
    private LocalDate dataRicezione;

    // Prezzo totale dell'ordine
    @Column(nullable = false)
    private Double prezzoTotale;

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

    // Molti acquisti possono essere fatti allo stesso fornitore. Il fornitore viene caricato quando richiesto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornitore_id", nullable = false)
    private Fornitore fornitore;

    // Un acquisto di attrezzatura è composto da molte righe, ciascuna corrispondente a un tipo di attrezzatura
    // diverso. Le righe vengono salvate/eliminate insieme all'ordine.
    @OneToMany(mappedBy = "acquistoAttrezzatura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RigaAcquistoAttrezzatura> righe;
}
