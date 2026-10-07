package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.RestituzioneDto;
import lidosunset.service.RestituzioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Il controller gestisce la restituzione delle attrezzature prenotate. È accessibile
// solo dall'admin e dall'istruttore. Esso chidue la prenotazione e, se viene segnalato
// un danno, apre una manutenzione urgente

@RestController
@RequestMapping("/api/restituzioni")
@PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
public class RestituzioneController {
    private final RestituzioneService restituzioneService;

    public RestituzioneController(RestituzioneService restituzioneService) {
        this.restituzioneService = restituzioneService;
    }

    // Permette di registrare la restituzione di un'attrezzatura
    @PostMapping
    public ResponseEntity<RestituzioneDto> effettuaRestituzione( @RequestBody RestituzioneDto dto){
        RestituzioneDto nuovaRestituzione = restituzioneService.effettuaRestituzione(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaRestituzione);
    }

    // Permette di modificare una restituzione esistente
    @PutMapping("/{id}")
    public ResponseEntity<RestituzioneDto> aggiornaRestituzione(@PathVariable Long id,@Valid @RequestBody RestituzioneDto dto){
        return restituzioneService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
