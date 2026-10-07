package lidosunset.service;

import lidosunset.config.CacheConfig;
import lidosunset.model.entity.TipoManutenzione;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import lidosunset.model.dto.ManutenzioneDto;
import lidosunset.model.entity.Attrezzatura;
import lidosunset.model.entity.Manutenzione;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.repository.AttrezzaturaRepository;
import lidosunset.repository.ManutenzioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// La classe gestisce la manutenzione delle attrezzature, inclusa l'apertura automatica di manutenzioni
// urgenti da danno (chiamata da RestituzioneService).

@Service
@RequiredArgsConstructor
public class ManutenzioneService {
    private final ManutenzioneRepository manutenzioneRepository;
    private final AttrezzaturaRepository attrezzaturaRepository;
    private final CacheManager cacheManager;


    // Recupera tutte le manutenzioni
    @Transactional(readOnly=true)
    public List<ManutenzioneDto> trovaTutte(){
        return manutenzioneRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Cerca le manutenzioni per ID
    @Transactional(readOnly=true)
    public Optional<ManutenzioneDto> trovaPerId(Long id){
        return manutenzioneRepository.findByIdAndAttivaTrue(id)
                .map(this::toDto);
    }

    // Crea una manutenzione (con lo stato attiva=true). Inoltre, svuota la cache se l'attrezzatura va in manutenzione
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#dto.attrezzaturaId", condition = "!#dto.completata"),
            @CacheEvict(value = "dashboardCache", allEntries = true)
    })
    @Transactional
    public ManutenzioneDto salva(ManutenzioneDto dto){
        // Si recupera l'attrezzatura
        Attrezzatura attrezzatura = attrezzaturaRepository.findByIdAndAttivaTrue(dto.getAttrezzaturaId())
                .orElseThrow(() -> new IllegalArgumentException("Attrezzatura non trovata con ID: "+dto.getAttrezzaturaId()));

        // Se la manutenzione viene aperta e non è completata, l'attrezzatura viene messa in stato IN_MANUTENZIONE
        if (!dto.isCompletata()){
            attrezzatura.setStato(StatoAttrezzatura.IN_MANUTENZIONE);
            attrezzaturaRepository.save(attrezzatura);
        }

        Manutenzione nuovaManutenzione = Manutenzione.builder()
                .data(dto.getData())
                .descrizione(dto.getDescrizione())
                .tipo(dto.getTipo())
                .completata(dto.isCompletata())
                .note(dto.getNote())
                .attrezzatura(attrezzatura)
                .attiva(true)
                .costo(dto.getCosto() != null ? dto.getCosto() : BigDecimal.ZERO)
                .build();

        return toDto(manutenzioneRepository.save(nuovaManutenzione));
    }


    // Aggiorna i dettagli di una manutenzione
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public Optional<ManutenzioneDto> aggiorna(Long id, ManutenzioneDto dto){
        return manutenzioneRepository.findByIdAndAttivaTrue(id)
                .map(esistente ->{
                    esistente.setData(dto.getData());
                    esistente.setDescrizione(dto.getDescrizione());
                    esistente.setTipo(dto.getTipo());
                    esistente.setCompletata(dto.isCompletata());
                    esistente.setNote(dto.getNote());
                    esistente.setCosto(dto.getCosto() != null ? dto.getCosto() : BigDecimal.ZERO);

                    // Se la manutenzione viene marcata come COMPLETATA, l'attrezzatura torna disponibile;
                    // altrimenti, resta IN_MANUTENZIONE
                    Attrezzatura attrezzatura =esistente.getAttrezzatura();
                    if (dto.isCompletata()){
                        attrezzatura.setStato(StatoAttrezzatura.DISPONIBILE);
                    } else {
                        attrezzatura.setStato(StatoAttrezzatura.IN_MANUTENZIONE);
                    }
                    attrezzaturaRepository.save(attrezzatura);
                    svuotaCacheAttrezzatura(attrezzatura.getId());
                    return toDto(manutenzioneRepository.save(esistente));
                });
    }

    // Permette di cancellare una manutenzione (in realtà non la elimina veramente ma setta il parametro attiva=false e non è visibile)
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public void elimina(Long id){
        manutenzioneRepository.findByIdAndAttivaTrue(id).ifPresent(manutenzione -> {
            // Setta il parametro attiva=false;
            manutenzione.setAttiva(false);

            // Se la manutenzione era ancora aperta (non completata), allora l'attrezzatura era IN_MANUTENZIONE;
            // eliminandola, l'attrezzatura torna disponibile
            if (!manutenzione.isCompletata()){
                Attrezzatura attrezzatura = manutenzione.getAttrezzatura();
                attrezzatura.setStato(StatoAttrezzatura.DISPONIBILE);
                attrezzaturaRepository.save(attrezzatura);

                svuotaCacheAttrezzatura(attrezzatura.getId());
            }

            manutenzioneRepository.save(manutenzione);
        });
    }

    // Metodo invocata dal RestituzioneService in caso di danni
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#attrezzatura.id"),
            @CacheEvict(value = "dashboardCache", allEntries = true)
    })
    @Transactional
    public void creaManutenzioneDaDanno(Attrezzatura attrezzatura, String descrizioneDanni){
        // Cambia lo stato dell'attrezzatura in IN_MANUTENZIONE
        attrezzatura.setStato(StatoAttrezzatura.IN_MANUTENZIONE);
        attrezzaturaRepository.save(attrezzatura);

        // Si crea la manutenzione straordinaria aperta.
        // Il costo è inizialmente 0 perché non è ancora noto al momento
        // della segnalazione: lo staff lo aggiornerà modificando la
        // manutenzione una volta nota la spesa di riparazione.
        Manutenzione manutenzionedanni = Manutenzione.builder()
                .data(java.time.LocalDate.now())
                .descrizione("DANNO SEGNALATO ALLA RICONSEGNA: "+descrizioneDanni)
                .tipo(TipoManutenzione.URGENTE)
                .completata(false)
                .note("Generata automaticamente dal sistema a seguito di danni riscontrati alla riconsegna")
                .attrezzatura(attrezzatura)
                .attiva(true)
                .costo(BigDecimal.ZERO)
                .build();

        manutenzioneRepository.save(manutenzionedanni);
    }

    // Svuota tramite il CacheManager la voce di cache di una specifica attrezzaura, usato ogni volta che una manutenzione nel cambia lo
    // stato, per non lasciare in cache un dato non più aggiornato.
    @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#idAttrezzatura")
    public void svuotaCacheAttrezzatura(Long idAttrezzatura){
        Cache cache = cacheManager.getCache(CacheConfig.ATTREZZATURA_BY_ID_CACHE);
        if (cache != null){
            cache.evict(idAttrezzatura);
        }
    }

    // Converte l'entity in DTO
    private ManutenzioneDto toDto(Manutenzione m){
        return ManutenzioneDto.builder()
                .id(m.getId())
                .data(m.getData())
                .descrizione(m.getDescrizione())
                .tipo(m.getTipo())
                .completata(m.isCompletata())
                .note(m.getNote())
                .costo(m.getCosto())
                .attrezzaturaId(m.getAttrezzatura().getId())
                .attrezzaturaNome(m.getAttrezzatura().getNome())
                .build();
    }
}