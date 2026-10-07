package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="fornitore")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fornitore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String partitaIva;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Column
    private String indirizzo;

    // Indica se il fornitore fornisce attrezzature sportive o prodotti per il ristorante e per il bar
    // I valori sono contenuti nell'enum SettoreFornitore
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettoreFornitore settore;

    @Column(nullable = false)
    @Builder.Default
    private boolean attiva=true;

    // Un fornitore può avere più acquisti di attrezzature. La relazione è gestita dal campo "fornitore" in AcquistoAttrezzatura.
    // Gli acquisti non vengono caricati finché non servono
    @OneToMany(mappedBy = "fornitore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AcquistoAttrezzatura> acquistiAttrezzature;

    // Un fornitore può avere più acquisti per il ristorante. La relazione è gestita dal campo "fornitore" in AcquistoAttrezzatura.
    // Gli acquisti non vengono caricati finché non servono
    @OneToMany(mappedBy = "fornitore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AcquistoRistorante> acquistiRistorante;




}
