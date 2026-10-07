package lidosunset.service;

import lidosunset.config.CacheConfig;
import lidosunset.model.dto.RestituzioneDto;
import lidosunset.model.entity.*;
import lidosunset.repository.AttrezzaturaRepository;
import lidosunset.repository.PrenotazioneRepository;
import lidosunset.repository.RestituzioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// La classe gestisce la restituzione delle attrezzature al termine della prenotazione, chiudendola
// e aprendo una manutenzione urgente in caso di danno (delegata a ManutenzioneService).
@Service
@RequiredArgsConstructor
public class RestituzioneService {
    private final RestituzioneRepository restituzioneRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final AttrezzaturaRepository attrezzaturaRepository;
    private final ManutenzioneService manutenzioneService;
    private final CacheManager cacheManager;

    // Registra una nuova restituzione
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public RestituzioneDto effettuaRestituzione(RestituzioneDto dto){
        // Si recupera la prenotazione associata
        Prenotazione prenotazione = prenotazioneRepository.findByIdAndAttivaTrue(dto.getPrenotazioneId())
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata con ID: "+dto.getPrenotazioneId()));

        // Si chiude la prenotazione, forzando con lo stato COMPLETATA
        prenotazione.setStato(StatoPrenotazione.COMPLETATA);
        prenotazioneRepository.save(prenotazione);

        RestituzioneAttrezzatura restituzione = RestituzioneAttrezzatura.builder()
                .data(dto.getData())
                .ora(dto.getOra())
                .danni(dto.isDanni())
                .descrizioneDanni(dto.getDescrizioneDanni())
                .prenotazione(prenotazione)
                .build();

        restituzioneRepository.save(restituzione);

        //Se ci sono danni, l'attrezzatuara va nello stato IN_MANUTENZIONE;
        // altrimenti, va nello stato DISPONIBILE
        Attrezzatura attrezzatura = prenotazione.getAttrezzatura();
        if (dto.isDanni()){
            manutenzioneService.creaManutenzioneDaDanno(attrezzatura, dto.getDescrizioneDanni());
        } else {
            attrezzatura.setStato(StatoAttrezzatura.DISPONIBILE);
            attrezzaturaRepository.save(attrezzatura);
        }
        svuotaCacheRestituzione(prenotazione.getId(), attrezzatura.getId());
        dto.setId(restituzione.getId());
        return dto;
    }

    // Aggiorna una restituzione esistente
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public Optional<RestituzioneDto> aggiorna(Long id, RestituzioneDto dto){
        return restituzioneRepository.findById(id)
                .map(esistente -> {
                    esistente.setData(dto.getData());
                    esistente.setOra(dto.getOra());
                    esistente.setDanni(dto.isDanni());
                    esistente.setDescrizioneDanni(dto.getDescrizioneDanni());

                    // Se il parametro danni=false, l'attrezzatura è nello stato DISPONIBILE
                    // altrimenti, è nello stato IN_MANUTENZIONE
                    Attrezzatura attrezzatura = esistente.getPrenotazione().getAttrezzatura();
                    if (dto.isDanni()){
                        attrezzatura.setStato(StatoAttrezzatura.IN_MANUTENZIONE);
                    } else {
                        attrezzatura.setStato(StatoAttrezzatura.DISPONIBILE);
                    }
                    attrezzaturaRepository.save(attrezzatura);
                    svuotaCacheRestituzione(esistente.getPrenotazione().getId(), attrezzatura.getId());
                    return toDto(restituzioneRepository.save(esistente));
                });
    }


    // Svuota tramite il CacheManager la cache per-id sia della prenotazione che dell'attrezzatura coinvolta per questa
    // restituzione.
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#idPrenotazione"),
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#idAttrezzatura")
    })
    public void svuotaCacheRestituzione(Long idPrenotazione, Long idAttrezzatura){
        Cache cachePrenotazione = cacheManager.getCache(CacheConfig.PRENOTAZIONE_BY_ID_CACHE);
        if (cachePrenotazione != null){
            cachePrenotazione.evict(idPrenotazione);
        }
        Cache cacheAttrezzatura = cacheManager.getCache(CacheConfig.ATTREZZATURA_BY_ID_CACHE);
        if (cacheAttrezzatura != null){
            cacheAttrezzatura.evict(idAttrezzatura);
        }
    }

    // Converte l'entity in DTO
    private RestituzioneDto toDto(RestituzioneAttrezzatura r){
        return RestituzioneDto.builder()
                .id(r.getId())
                .data(r.getData())
                .ora(r.getOra())
                .danni(r.isDanni())
                .descrizioneDanni(r.getDescrizioneDanni())
                .prenotazioneId(r.getPrenotazione().getId())
                .build();
    }
}
