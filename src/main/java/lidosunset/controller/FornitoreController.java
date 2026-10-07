package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.FornitoreDto;
import lidosunset.service.FornitoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Il controller gestisce l'anagrafica dei fornitori. Le letture sono consenti sia ad admin che al barista;
// mentre, le richieste riservate solo all'ADMIN.

@RestController
@RequestMapping("/api/fornitori")
@RequiredArgsConstructor
public class FornitoreController {
    private final FornitoreService fornitoreService;

    // Recupera la lista di tutti i fornitori attivi - anche il barista può
    // consultarla per scegliere il fornitore quando registra un acquisto
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARISTA')")
    public ResponseEntity<List<FornitoreDto>> trovaTutti(){
        return ResponseEntity.ok(fornitoreService.trovaTutti());
    }

    // Recupera il dettaglio di un singolo fornitore per ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARISTA')")
    public ResponseEntity<FornitoreDto> trovaPerId(@PathVariable Long id){
        return fornitoreService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ***Metodi accessibili solo dall'ADMIN***

    // Crea una nuova anagrafica fornitore
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FornitoreDto> crea(@Valid @RequestBody FornitoreDto dto){
        FornitoreDto nuovoFornitore = fornitoreService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovoFornitore);
    }

    // Aggiorna i dati di un fornitore esistente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FornitoreDto> aggiorna(@PathVariable Long id, @Valid @RequestBody FornitoreDto dto){
        return fornitoreService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina un fornitore (in realtà setta il parametro attiva=false)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> elimina(@PathVariable Long id){
        boolean eliminato = fornitoreService.elimina(id);
        return eliminato ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}