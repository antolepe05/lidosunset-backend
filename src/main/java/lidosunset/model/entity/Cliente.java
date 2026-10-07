package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="cliente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Builder.Default
    @Column(nullable = false)
    private boolean attiva=true;

    // Si usa @OneToMany perché un cliente può avere molte prenotazioni;
    // Si usa mappedBy="cliente" perché la relazione è gestita dal campo "cliente" in Prenotazione;
    // Si usa cascade=CascadeType.ALL perché le operazioni su Cliente si propagano alle Prenotazioni;
    // Si usa fetch=FetcType.LAZY in modo che le prenotazioni vengano caricate dal DB solo quando richiesto.
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prenotazione> prenotazioni;
}
