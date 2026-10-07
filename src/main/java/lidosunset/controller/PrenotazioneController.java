package lidosunset.controller;

import jakarta.validation.Valid;
import lidosunset.model.dto.PrenotazioneDto;
import lidosunset.service.PrenotazioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;



// Il controller gestisce le prenotazioni. Sono presenti due famiglie di endpoint: "/mie/*" che gestisce le prenotazione del cliente
// specifico; e per lo staff per qualunque prenotazione

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {
    private final PrenotazioneService prenotazioneService;

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    // Il cliente vede SOLO le sue prenotazioni
    @GetMapping("/mie")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PrenotazioneDto> getMiePrenotazioni(Principal principal){
        String email = principal.getName();
        return prenotazioneService.trovaPerEmailCliente(email);
    }

    // Dettaglio di ogni singola prenotazione del cliente loggato
    @GetMapping("/mie/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PrenotazioneDto> getMiaPrenotazione(@PathVariable Long id, Principal principal){
        return prenotazioneService.trovaMiaPerId(id, principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea una nuova prenotazione personale
    @PostMapping("/mie")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PrenotazioneDto> createMiaPrenotazione(@Valid @RequestBody PrenotazioneDto dto, Principal principal) {
        PrenotazioneDto salvata = prenotazioneService.salvaMia(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvata);
    }

    // Modifica i dettagli di una propria prenotazione
    @PutMapping("/mie/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PrenotazioneDto> updateMiaPrenotazione(@PathVariable Long id,@Valid @RequestBody PrenotazioneDto dto, Principal principal) {
        return prenotazioneService.aggiornaMia(id, dto, principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Annulla una propria prenotazione
    @DeleteMapping("/mie/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> annullaMiaPrenotazione(@PathVariable Long id, Principal principal) {
        boolean annullata = prenotazioneService.annullaMia(id, principal.getName());
        if (annullata) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Lista di tutte le prenotazioni del lido
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public List<PrenotazioneDto> getAll(){
        return prenotazioneService.trovaTutte();
    }

    // Dettaglio di ogni singola prenotazione per id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ISTRUTTORE')")
    public ResponseEntity<PrenotazioneDto> getById(@PathVariable Long id){
        return prenotazioneService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea una nuova prenotazione per conto di un cliente
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<PrenotazioneDto> createByStaff(@Valid @RequestBody PrenotazioneDto dto) {
        PrenotazioneDto salvata = prenotazioneService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvata);
    }

    // Lo staff modifica una prenotazione di un cliente
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<PrenotazioneDto> updateByStaff( @PathVariable Long id,@Valid @RequestBody PrenotazioneDto dto) {
        return prenotazioneService.trovaPerId(id)
                .map(esistente -> {
                    dto.setId(id);
                    PrenotazioneDto aggiornata = prenotazioneService.salva(dto);
                    return ResponseEntity.ok(aggiornata);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Lo staff annulla la prenotazione di un cliente
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<Void> annullaByStaff(@PathVariable Long id) {
        return prenotazioneService.trovaPerId(id)
                .map(esistente -> {
                    prenotazioneService.annulla(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}