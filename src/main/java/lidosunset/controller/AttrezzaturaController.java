package lidosunset.controller;

import lidosunset.model.dto.AttrezzaturaDto;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.TipoAttrezzatura;
import lidosunset.service.AttrezzaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// Il controller gestisce il catalogo delle attrezzature. Nei metodi getById() e getByTipo(), quando chi fa la
// richiesta è un cliente, invece di restituire il DTO completo dell'attrezzatura (che include il
// campo stato), il controller ne costruisce una copia ma con lo stato settato a null. Il cliente così
// vede che esiste una specifica attrezzatura ma non vede lo stato. Questo avviene perché
// i dati sono cachati nel service per non interrogare il DB ad ogni richiesta. Il controllo
// vero avviene al momento della prenotazione in PrenotazioneService.salva(), tramite isAttrezzaturaOccupata(),
// che interroga il DB direttamente.

@RestController
@RequestMapping("/api/attrezzature")
public class AttrezzaturaController {
    private final AttrezzaturaService attrezzaturaService;

    public AttrezzaturaController(AttrezzaturaService attrezzaturaService) {
        this.attrezzaturaService = attrezzaturaService;
    }

    // Lista completa di tutto il catalogo - SOLO STAFF
    @GetMapping
    @PreAuthorize("hasAnyRole('ISTRUTTORE', 'ADMIN')")
    public List<AttrezzaturaDto> getAll(){
        return attrezzaturaService.trovaTutte();
    }

    // Dettaglio di una singola attrezzatura - ACCESSIBILE MA MASCHERATO PER IL CLIENTE
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ISTRUTTORE', 'ADMIN')")
    public ResponseEntity<AttrezzaturaDto> getById(@PathVariable Long id, Authentication authentication){
        return attrezzaturaService.trovaPerId(id)
                .map(dto -> {
                    // Se l'utente loggato è un cliente, cloniamo il DTO nascondendo lo stato
                    if (hasRole(authentication, "ROLE_CLIENTE")) {
                        return AttrezzaturaDto.builder()
                                .id(dto.getId())
                                .nome(dto.getNome())
                                .tipo(dto.getTipo())
                                .descrizione(dto.getDescrizione())
                                .stato(null) // Oscurato
                                .build();
                    }
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Filtra per stato dell'oggetto - SOLO STAFF
    @GetMapping("/stato/{stato}")
    @PreAuthorize("hasAnyRole('ISTRUTTORE', 'ADMIN')")
    public List<AttrezzaturaDto> getByStato(@PathVariable StatoAttrezzatura stato){
        return attrezzaturaService.trovaPerStato(stato);
    }

    // Filtra per tipo - I clienti vedono cosa c'è, ma NON lo stato (Evita corruzione della cache)
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ISTRUTTORE', 'ADMIN')")
    public List<AttrezzaturaDto> getByTipo(@PathVariable TipoAttrezzatura tipo, Authentication authentication){
        List<AttrezzaturaDto> listaOriginale = attrezzaturaService.trovaPerTipo(tipo);

        // Se è un cliente, mappiamo i record in nuovi oggetti per preservare intatta la cache in memoria
        if (hasRole(authentication, "ROLE_CLIENTE")) {
            return listaOriginale.stream()
                    .map(dto -> AttrezzaturaDto.builder()
                            .id(dto.getId())
                            .nome(dto.getNome())
                            .tipo(dto.getTipo())
                            .descrizione(dto.getDescrizione())
                            .stato(null) // Oscurato in modo sicuro
                            .build())
                    .toList();
        }
        return listaOriginale;
    }

    // Filtra le attrezzature per tipo e per stato - SOLO STAFF
    @GetMapping("/filtro")
    @PreAuthorize("hasAnyRole('ISTRUTTORE', 'ADMIN')")
    public List<AttrezzaturaDto> getByTipoEStato(@RequestParam TipoAttrezzatura tipo, @RequestParam StatoAttrezzatura stato){
        return attrezzaturaService.trovaPerTipoEStato(tipo, stato);
    }

    // Aggiunge un nuovo pezzo al catalogo delle attrezzature
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<AttrezzaturaDto> create(@RequestBody AttrezzaturaDto dto){
        AttrezzaturaDto salvata = attrezzaturaService.salva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvata);
    }

    // Modifica una attrezzatura
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<AttrezzaturaDto> update(@PathVariable Long id, @RequestBody AttrezzaturaDto dto){
        return attrezzaturaService.trovaPerId(id)
                .map(esistente -> {
                    dto.setId(id);
                    AttrezzaturaDto aggiornata = attrezzaturaService.salva(dto);
                    return ResponseEntity.ok(aggiornata);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Rimuove una attrezzatura dal DB
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ISTRUTTORE')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        return attrezzaturaService.trovaPerId(id)
                .map(esistente -> {
                    attrezzaturaService.elimina(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Metodo helper per estrarre in sicurezza le Authorities di Spring Security
    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}