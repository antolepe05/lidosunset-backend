package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.AcquistoRistoranteDto;
import lidosunset.model.entity.AcquistoRistorante;
import lidosunset.service.AcquistoRistoranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// IL controller è utilizzato per l'acquisto dei prodotti del ristorante/bar
// (ordine ai fornitori di ristorazione). Il barista ha l'accesso completo, oltre all'ADMIN
@RestController
@RequestMapping("/api/acquisti-ristorante")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BARISTA')")
public class AcquistoRistoranteController {

    private final AcquistoRistoranteService acquistoRistoranteService;

    // Elenco completo degli acquisti
    @GetMapping
    public ResponseEntity<List<AcquistoRistoranteDto>> trovaTutti(){
        return ResponseEntity.ok(acquistoRistoranteService.trovaTutti());
    }

    // Dettaglio di un singolo ordine con le relative righe
    @GetMapping("/{id}")
    public ResponseEntity<AcquistoRistoranteDto> trovaPerId(@PathVariable Long id){
        return acquistoRistoranteService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Viene creato un nuovo ordine
    @PostMapping
    public ResponseEntity<AcquistoRistoranteDto> crea(@Valid @RequestBody AcquistoRistoranteDto dto){
        AcquistoRistoranteDto nuovoAcquisto = acquistoRistoranteService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovoAcquisto);
    }

    // Viene modificato un ordine esistente
    @PutMapping("/{id}")
    public ResponseEntity<AcquistoRistoranteDto> aggiorna(@PathVariable Long id, @Valid @RequestBody AcquistoRistoranteDto dto){
        return acquistoRistoranteService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina un ordine (in realtà viene settato il parametro attiva=false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id){
        boolean eliminato = acquistoRistoranteService.elimina(id);
        return eliminato ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
