package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="attrezzatura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attrezzatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Indica il nome dell'attrezzatura
    @Column(nullable = false)
    private String nome;

    // Indica il tipo dell'attrezzatura
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAttrezzatura tipo;


    // Indica lo stato dell'attrezzatura dove i valori possibili sono definiti nell'enum StatoAttrezzatura
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoAttrezzatura stato;

    @Column
    private String descrizione;

    @Builder.Default
    @Column(nullable = false)
    private boolean attiva = true;

    @Version
    private Integer version;

    // Un'attrezzatura può avere molte prenotazioni nel tempo. Si usa mappedBy="attrezzatura" perché
    // la relazione è gestita dal campo "attrezzatura" in Prenotazione. Inoltre, le prenotazioni vengono
    // caricate solo quando richiesto.
    @OneToMany(mappedBy = "attrezzatura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prenotazione> prenotazioni;

    // Un'attrezzatura può avere molte manutenzioni nel tempo
    @OneToMany(mappedBy = "attrezzatura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Manutenzione> manutenzioni;
}
