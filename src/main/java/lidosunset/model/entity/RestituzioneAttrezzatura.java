package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="restituzione_attrezzatura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestituzioneAttrezzatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data in cui è avvenuta la restituzione
    @Column(nullable = false)
    private LocalDate data;

    // Ora in cui è avvenuta la restituzione
    @Column(nullable = false)
    private LocalTime ora;

    // Indica se l'attrezzatura presenta danni. Se è true viene generata una ManutenzioneUrgente
    @Column(nullable = false)
    private boolean danni;

    // È opzionale. Viene usato solo se danni=true
    @Column
    private String descrizioneDanni;

    // Una restituzione è associata ad una sola prenotazione. La prenotazione viene caricata solo quando viene esplicitamente richiesta.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="prenotazione_id", nullable = false)
    private Prenotazione prenotazione;
}
