package lidosunset.service;

import lidosunset.model.dto.AcquistoAttrezzaturaDto;
import lidosunset.model.dto.RigaAcquistoAttrezzaturaDto;
import lidosunset.model.entity.AcquistoAttrezzatura;
import lidosunset.model.entity.Fornitore;
import lidosunset.model.entity.RigaAcquistoAttrezzatura;
import lidosunset.repository.AcquistoAttrezzaturaRepository;
import lidosunset.repository.FornitoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// La classe contiene la logica di business per gli acquisti di attrezzature con le relative righe
// (più righe per ogni ordine).

@Service
@RequiredArgsConstructor
public class AcquistoAttrezzaturaService {
    private final AcquistoAttrezzaturaRepository acquistoAttrezzaturaRepository;
    private final FornitoreRepository fornitoreRepository;

    // Recupera tutti gli acquisti
    @Transactional(readOnly = true)
    public List<AcquistoAttrezzaturaDto> trovaTutti(){
        return acquistoAttrezzaturaRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Cerca un acquisto per ID
    @Transactional(readOnly = true)
    public Optional<AcquistoAttrezzaturaDto> trovaPerId(Long id){
        return acquistoAttrezzaturaRepository.findByIdAndAttivaTrue(id)
                .map(this::toDto);
    }

    // Registra un nuovo acquisto di attrezzatura
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public AcquistoAttrezzaturaDto salva(AcquistoAttrezzaturaDto dto){
        // Verifca che il fornitore esista e sia attivo
        Fornitore fornitore = fornitoreRepository.findByIdAndAttivaTrue(dto.getFornitoreId())
                .orElseThrow(() -> new IllegalArgumentException("Fornitore non trovato con ID: "+dto.getFornitoreId()));

        AcquistoAttrezzatura nuovoAcquisto = toEntity(dto);
        nuovoAcquisto.setFornitore(fornitore);
        nuovoAcquisto.setAttiva(true);

        // Assoccia ogni riga all'acquisto principale
        if (nuovoAcquisto.getRighe() != null){
            nuovoAcquisto.getRighe().forEach(riga -> riga.setAcquistoAttrezzatura(nuovoAcquisto));
        }

        return toDto(acquistoAttrezzaturaRepository.save(nuovoAcquisto));
    }

    // Aggiorna i dettagli di un acquisto esistente
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public Optional<AcquistoAttrezzaturaDto> aggiorna(Long id, AcquistoAttrezzaturaDto dto){
        return acquistoAttrezzaturaRepository.findByIdAndAttivaTrue(id)
                .map(esistente -> {
                    // Verifica se il fornitore cambia nell'aggiornamento, per evitare una query inutile quando resta lo stesso
                    if (!esistente.getFornitore().getId().equals(dto.getFornitoreId())){
                        Fornitore nuovoFornitore = fornitoreRepository.findByIdAndAttivaTrue(dto.getFornitoreId())
                                .orElseThrow(() -> new IllegalArgumentException("Nuovo fornitore non trovato con ID: "+dto.getFornitoreId()));
                        esistente.setFornitore(nuovoFornitore);
                    }

                    esistente.setDataOrdine(dto.getDataOrdine());
                    esistente.setDataRicezione(dto.getDataRicezione());
                    esistente.setPrezzoTotale(dto.getPrezzoTotale());
                    esistente.setStato(dto.getStato());
                    esistente.setNote(dto.getNote());

                    // Svuota le righe esistenti (o inizializza la lista se era null) prima di rimetterle tutte
                    if (esistente.getRighe() != null){
                        esistente.getRighe().clear();
                    } else {
                        esistente.setRighe(new ArrayList<>());
                    }
                    // Ricostruisce da zero le righe con i dati aggiornati
                    if (dto.getRighe() != null){
                        List<RigaAcquistoAttrezzatura> nuoveRighe = dto.getRighe().stream()
                                .map(this::toRigaEntity)
                                .toList();
                        nuoveRighe.forEach(riga -> {
                            riga.setAcquistoAttrezzatura(esistente);
                            esistente.getRighe().add(riga);
                        });
                    }
                    return toDto(acquistoAttrezzaturaRepository.save(esistente));
                });
    }

    // Elimina l'acquisto (ma in realtà setta attiva=false)
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public boolean elimina(Long id){
        return acquistoAttrezzaturaRepository.findByIdAndAttivaTrue(id)
                .map(acquisto -> {
                    acquisto.setAttiva(false);
                    acquistoAttrezzaturaRepository.save(acquisto);
                    return true;
                }).orElse(false);
    }

    // Converte l'entity in DTO per l'esposizione verso l'esterno
    private AcquistoAttrezzaturaDto toDto(AcquistoAttrezzatura entity){
        if (entity == null) return null;

        // Se non ci sono righe (entity nuova/vuota) usa una lista vuota invece di null
        List<RigaAcquistoAttrezzaturaDto> righeDto = entity.getRighe() !=null ?
                entity.getRighe().stream().map(this::toRigaDto).toList() : new ArrayList<>();

        return AcquistoAttrezzaturaDto.builder()
                .id(entity.getId())
                .dataOrdine(entity.getDataOrdine())
                .dataRicezione(entity.getDataRicezione())
                .prezzoTotale(entity.getPrezzoTotale())
                .stato(entity.getStato())
                .note(entity.getNote())
                .attiva(entity.isAttiva())
                .fornitoreId(entity.getFornitore().getId())
                .righe(righeDto)
                .build();
    }

    // Converte il DTO per la persistenza
    private AcquistoAttrezzatura toEntity(AcquistoAttrezzaturaDto dto){
        if (dto == null) return null;

        // Se non ci sono righe (entity nuova/vuota) usa una lista vuota invece di null
        List<RigaAcquistoAttrezzatura> righeEntity = dto.getRighe() != null ?
                new ArrayList<>(dto.getRighe().stream().map(this::toRigaEntity).toList()) : new ArrayList<>();

        return AcquistoAttrezzatura.builder()
                .id(dto.getId())
                .dataOrdine(dto.getDataOrdine())
                .dataRicezione(dto.getDataRicezione())
                .prezzoTotale(dto.getPrezzoTotale())
                .stato(dto.getStato())
                .note(dto.getNote())
                .attiva(dto.isAttiva())
                .righe(righeEntity)
                .build();
    }

    // Converte una singola riga da entity in DTO.
    private RigaAcquistoAttrezzaturaDto toRigaDto(RigaAcquistoAttrezzatura entity){
        if (entity == null) return null;
        return RigaAcquistoAttrezzaturaDto.builder()
                .id(entity.getId())
                .tipoAttrezzatura(entity.getTipoAttrezzatura())
                .quantita(entity.getQuantita())
                .prezzoUnitario(entity.getPrezzoUnitario())
                .prezzoTotale(entity.getPrezzoTotale())
                .note(entity.getNote())
                .build();
    }

    // Convere una singola riga da DTO ad entity
    private RigaAcquistoAttrezzatura toRigaEntity(RigaAcquistoAttrezzaturaDto dto){
        if (dto == null) return null;
        return RigaAcquistoAttrezzatura.builder()
                .id(dto.getId())
                .tipoAttrezzatura(dto.getTipoAttrezzatura())
                .quantita(dto.getQuantita())
                .prezzoUnitario(dto.getPrezzoUnitario())
                .prezzoTotale(dto.getPrezzoTotale())
                .note(dto.getNote())
                .build();
    }
}