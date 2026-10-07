package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.AcquistoAttrezzaturaDto;
import lidosunset.service.AcquistoAttrezzaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// Controller utilizzato per gli acquisti di attrezzature (ordini ai fornitori sportivi).
// Tutto il controllo è riservato all'ADMIN.

@RestController
@RequestMapping("/api/acquisti-attrezzature")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AcquistoAttrezzaturaController {
    private final AcquistoAttrezzaturaService acquistoAttrezzaturaService;

    // Recupera tutti gli ordini attivi
    @GetMapping
    public ResponseEntity<List<AcquistoAttrezzaturaDto>> trovaTutti(){
        return ResponseEntity.ok(acquistoAttrezzaturaService.trovaTutti());
    }

    // Dettaglio di un singolo aacquisto con le sue righe
    @GetMapping("/{id}")
    public ResponseEntity<AcquistoAttrezzaturaDto> trovaPerId(@PathVariable Long id){
        return acquistoAttrezzaturaService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra un nuovo acquisto di attrezzatura (con le relative righe)
    @PostMapping
    public ResponseEntity<AcquistoAttrezzaturaDto> crea(@Valid @RequestBody AcquistoAttrezzaturaDto dto){
        AcquistoAttrezzaturaDto nuovoAcquisto = acquistoAttrezzaturaService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovoAcquisto);
    }

    // Aggiorna dati e righe di un acquisto esistente
    @PutMapping("/{id}")
    public ResponseEntity<AcquistoAttrezzaturaDto> aggiorna(@PathVariable Long id, @Valid @RequestBody AcquistoAttrezzaturaDto dto){
        return acquistoAttrezzaturaService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina un acquisto (in realtà setta attiva=false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id){
        boolean eliminato = acquistoAttrezzaturaService.elimina(id);
        return eliminato ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
