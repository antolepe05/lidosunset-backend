package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.ManutenzioneDto;
import lidosunset.service.ManutenzioneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Il controller gestisce le manutenzioni delle attrezzature. Ogni metodo è accessibile
// sia dall'istruttore che dall'admin tranne il metodo delete() che è accessibile solo all'utente


@RestController
@RequestMapping("/api/manutenzioni")
@RequiredArgsConstructor
// Solo utenti con ruolo ADMIN e ISTRUTTORE possono interagire con il controller
@PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
public class ManutenzioneController {
    private final ManutenzioneService manutenzioneService;

    // Restituisce la lista con tutte le prenotazioni (attive e passate)
    @GetMapping
    public ResponseEntity<List<ManutenzioneDto>> trovaTutte(){
        return ResponseEntity.ok(manutenzioneService.trovaTutte());
    }

    // Si ottiene il dettaglio di una singola manutenzione
    @GetMapping("/{id}")
    public ResponseEntity<ManutenzioneDto> trovaId(@PathVariable Long id){
        return manutenzioneService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea una nuova manutenzione. L'annotazione @Valid serve a validare i vincoli dei campi del DTO.
    @PostMapping
    public ResponseEntity<ManutenzioneDto> crea(@Valid @RequestBody ManutenzioneDto dto){
        ManutenzioneDto nuovaManutenzione = manutenzioneService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaManutenzione);
    }

    // Sovrascrive o modifica una manutenzione
    @PutMapping("/{id}")
    public ResponseEntity<ManutenzioneDto> aggiorna(@PathVariable Long id, @Valid @RequestBody ManutenzioneDto dto){
        return manutenzioneService.aggiorna(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Elimina una manutenzione (in realtà non viene eliminato ma setta il valore attiva=false e non è visibile)
    // L'operazione di delete può essere eseguita dall'utente ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        return manutenzioneService.trovaPerId(id)
                .map(esistente -> {
                    manutenzioneService.elimina(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
