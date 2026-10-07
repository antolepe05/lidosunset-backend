package lidosunset.service;

import lidosunset.config.CacheConfig;
import lidosunset.model.dto.AttrezzaturaDto;
import lidosunset.model.entity.Attrezzatura;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.StatoPrenotazione;
import lidosunset.model.entity.TipoAttrezzatura;
import lidosunset.repository.AttrezzaturaRepository;
import lidosunset.repository.PrenotazioneRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// La classe gestisce il catalogo delle attrezzature, comprese la ricerca per stato/tipo e prevede, inoltre,
// l'eliminazione a cascate delle prenotazioni collegate. Il metodo elimina() richiama
// PrenotazioneService.annulla() per ciascuna prenotazione coinvolta, invece di modificare le entity direttamente
// e permette di invalidare la cache

@Service
public class AttrezzaturaService {
    private final AttrezzaturaRepository attrezzaturaRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final PrenotazioneService prenotazioneService;

    public AttrezzaturaService(AttrezzaturaRepository attrezzaturaRepository, PrenotazioneRepository prenotazioneRepository, PrenotazioneService prenotazioneService) {
        this.attrezzaturaRepository = attrezzaturaRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.prenotazioneService = prenotazioneService;
    }

    @Transactional(readOnly = true)
    public List<AttrezzaturaDto> trovaTutte(){
        return attrezzaturaRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // NOTA: Spring disimballa automaticamente l'Optional prima di valutare "unless",
    // quindi #result è già l'AttrezzaturaDto (o null), non l'Optional.
    @Cacheable(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<AttrezzaturaDto> trovaPerId(Long id){
        return attrezzaturaRepository.findByIdAndAttivaTrue(id).map(this::toDto);
    }

    // Filtra le attrezzature per stato
    @Transactional(readOnly = true)
    public List<AttrezzaturaDto> trovaPerStato(StatoAttrezzatura stato){
        return attrezzaturaRepository.findByStatoAndAttivaTrue(stato).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Filtra le attrezzature per tipo
    @Transactional(readOnly = true)
    public List<AttrezzaturaDto> trovaPerTipo(TipoAttrezzatura tipo){
        return attrezzaturaRepository.findByTipoAndAttivaTrue(tipo).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Filtra le prenotazioni per tipo e stato
    @Transactional(readOnly = true)
    public List<AttrezzaturaDto> trovaPerTipoEStato(TipoAttrezzatura tipo, StatoAttrezzatura stato){
        return attrezzaturaRepository.findByTipoAndStatoAndAttivaTrue(tipo, stato).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Al salvataggio svuota i dettagli ID e l'intera cache dei contatori dashboard
    @CacheEvict(value = {CacheConfig.ATTREZZATURA_BY_ID_CACHE, "dashboardCache"}, allEntries = true)
    @Transactional
    public AttrezzaturaDto salva(AttrezzaturaDto dto){
        Attrezzatura entity;

        // Se il DTO ha già un Id, è una modifica, quindi, si carica l'esistente e si modificano i campi.
        // Altrimenti è una nuova creazione da zero
        if(dto.getId() != null) {
            entity = attrezzaturaRepository.findByIdAndAttivaTrue(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Attrezzatura non trovata con ID: " + dto.getId()));

            entity.setNome(dto.getNome());
            entity.setTipo(dto.getTipo());
            entity.setStato(dto.getStato());
            entity.setDescrizione(dto.getDescrizione());
        } else {
            entity = toEntity(dto);
        }
        Attrezzatura salvata = attrezzaturaRepository.save(entity);
        return toDto(salvata);
    }

    // All'eliminazione svuota i dettagli ID e l'intera cache dei contatori dashboard
    @CacheEvict(value = {CacheConfig.ATTREZZATURA_BY_ID_CACHE, "dashboardCache"}, allEntries = true)
    @Transactional
    public void elimina(Long id){
        attrezzaturaRepository.findByIdAndAttivaTrue(id).ifPresent(attrezzatura -> {
            attrezzatura.setAttiva(false);
            attrezzaturaRepository.save(attrezzatura);

            // Tutte le prenotazioni attive su questa attrezzatura vengono annullate
            prenotazioneRepository.findByAttrezzaturaIdAndAttivaTrue(id).forEach(prenotazione -> {
                prenotazione.setAttiva(false);
                prenotazione.setStato(StatoPrenotazione.ANNULLATA);
                prenotazioneRepository.save(prenotazione);
            });
        });
    }

    // Converte l'entity in DTO
    private AttrezzaturaDto toDto(Attrezzatura entity){
        if (entity == null) return null;
        return AttrezzaturaDto.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .tipo(entity.getTipo())
                .stato(entity.getStato())
                .descrizione(entity.getDescrizione())
                .build();
    }

    // Converte il DTO in entity
    private Attrezzatura toEntity(AttrezzaturaDto dto){
        if (dto == null) return null;
        Attrezzatura entity = new Attrezzatura();
        entity.setId(dto.getId());
        entity.setNome(dto.getNome());
        entity.setTipo(dto.getTipo());
        entity.setStato(dto.getStato());
        entity.setDescrizione(dto.getDescrizione());
        return entity;
    }
}