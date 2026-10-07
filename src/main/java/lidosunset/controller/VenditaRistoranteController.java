package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.VenditaRistoranteDto;
import lidosunset.service.VenditaRistoranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Il controller registra le vendite al ristorante (incasso). L'accesso è cosnentito solo
// all'admin.

@RestController
@RequestMapping("/api/vendite-ristorante")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VenditaRistoranteController {
    private final VenditaRistoranteService venditaRistoranteService;

    // Recupera tutte le vendite attive
    @GetMapping
    public ResponseEntity<List<VenditaRistoranteDto>> trovaTutte(){
        return ResponseEntity.ok(venditaRistoranteService.trovaTutte());
    }

    // Dettaglio di una singola vendita
    @GetMapping("/{id}")
    public ResponseEntity<VenditaRistoranteDto> trovaPerId(@PathVariable Long id){
        return venditaRistoranteService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra una nuova vendita
    @PostMapping
    public ResponseEntity<VenditaRistoranteDto> crea(@Valid @RequestBody VenditaRistoranteDto dto){
        VenditaRistoranteDto nuovaVendita = venditaRistoranteService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaVendita);
    }

    // Aggiorna una vendita esistente
    @PutMapping("/{id}")
    public ResponseEntity<VenditaRistoranteDto> aggiorna(@PathVariable Long id, @Valid @RequestBody VenditaRistoranteDto dto){
        return venditaRistoranteService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina una vendita (in realtà setta attiva=false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id){
        boolean eliminato = venditaRistoranteService.elimina(id);
        return eliminato ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}