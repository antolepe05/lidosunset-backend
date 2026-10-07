package lidosunset.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vendite_ristorante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenditaRistorante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private BigDecimal totale;

    private String note;

    @Column(nullable = false)
    @Builder.Default
    private boolean attiva = true;
}
