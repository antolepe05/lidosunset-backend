package lidosunset.controller;

import lidosunset.model.dto.ClienteDto;
import lidosunset.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


// Il controller gestisce l'anagrafica dei clienti, con tre livelli di permesso: il cliente stesso
// (accede ai dati del proprio profilo, admin e istruttore possono fare la ricerca
// e la lettura di un cliente per creare prenotazioni per conto di un cliente; mentre solo l'ADMIN
// può creare un profilo di un cliente ed eliminarlo e mdoificare i dati del cliente).

@RestController
@RequestMapping("/api/clienti")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Mostra i dati del cliente attualmente loggato. Spring estrae l'utente
    // dal contesto di sicurezza
    @GetMapping("/profilo")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteDto> getMioProfilo(Principal principal){
        String email = principal.getName();
        return clienteService.trovaPerEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lista completa di tutti i clienti - anche l'istruttore può consultarla
    // per scegliere il cliente quando crea una prenotazione, ma non può
    // creare/modificare/eliminare anagrafiche
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public List<ClienteDto> getAll(){
        return clienteService.trovaTutti();
    }

    // Dettaglio di un singolo cliente tramite ID specifico
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<ClienteDto> getById(@PathVariable Long id){
        return clienteService.trovaPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Filtro di ricerca per cognome - utile anche per l'istruttore
    @GetMapping("/cerca/cognome")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public List<ClienteDto> getByCognome(@RequestParam String cognome){
        return clienteService.trovaByCognome(cognome);
    }

    // Ricerca live per nome o cognome, usata nel form di creazione prenotazione
    @GetMapping("/cerca")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public List<ClienteDto> cerca(@RequestParam String query){
        return clienteService.cercaPerNomeOCognome(query);
    }

    // ***Metodi accessibili solo dall'ADMIN***

    // Lista paginata con metadata
    @GetMapping("/paginate")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ClienteDto> getPaginate(@PageableDefault(size = 5, page=0) Pageable pageable){
        return clienteService.trovaPaginati(pageable);
    }

    // Registrazione di un nuovo cliente
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteDto> create(@RequestBody ClienteDto clienteDto){
        ClienteDto salvato = clienteService.salva(clienteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvato);
    }

    // Aggiornamento completo dei dati di un cliente esistente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteDto> update(@PathVariable Long id, @RequestBody ClienteDto clienteDto){
        return clienteService.trovaPerId(id)
                .map(esistente -> {
                    clienteDto.setId(id);
                    ClienteDto aggiornato = clienteService.salva(clienteDto);
                    return ResponseEntity.ok(aggiornato);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminazione di un cliente dal DB (in realtà non viene completamente eliminato
    // ma disattivato)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        return clienteService.trovaPerId(id)
                .map(cliente -> {
                    clienteService.elimina(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}