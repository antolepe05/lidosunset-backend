package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="prenotazione")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data della prenotazione
    @Column(nullable = false)
    private LocalDate data;

    // Ora di inizio della prenotazione
    @Column(nullable = false)
    private LocalTime oraInizio;

    // Ora di fine della prenotazione
    @Column(nullable = false)
    private LocalTime oraFine;

    // Indica lo stato della prenotazione dove i valori sono definiti nell'enum StatoPrenotazione
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPrenotazione stato;

    // Note opzionali sulla prenotazione
    @Column
    private String note;

    @Builder.Default
    @Column(nullable = false)
    private boolean attiva=true;

    @Column(nullable = false)
    private BigDecimal prezzo;

    @Version
    private Integer version;

    // Molte prenotazioni appartengono ad un solo cliente.
    // @JoinColumn specifica la colonna della chiave esterna nel DB.
    // Il cliente non viene caricato finché non viene esplicitamente richiesto
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;


    // Molte prenotazioni possono riguardare la stessa attrezzatura (in momenti diversi)
    // @JoinColumn specifica la calonna della chiave esterna nel DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attrezzatura_id", nullable = false)
    private Attrezzatura attrezzatura;
}
