package lidosunset.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="manutenzione")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manutenzione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data in cui è stata eseguita o pianificata la manutenzione
    @Column(nullable = false)
    private LocalDate data;

    // Descrizione dell'intervento di manutenzione
    @Column(nullable = false)
    private String descrizione;

    // Indica il tipo di manutenzione dove i valori sono nell'enum TipoManutenzione
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoManutenzione tipo;

    //Indica se la manutenzione è stata completata o è ancora in corso
    @Column(nullable = false)
    private boolean completata;

    @Column
    private String note;

    @Builder.Default
    @Column(nullable = false)
    private boolean attiva=true;

    @Column(nullable = false)
    private BigDecimal costo;

    // Molte manutenzioni possono riguardare la stessa attrezzatura.
    // @JoinColumn specifica la colonna della chiave esterna nel DB.
    // L'attrezzatura non viene caricata finché non viene esplicitamente richiesta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attrezzatura_id", nullable = false)
    private Attrezzatura attrezzatura;
}
