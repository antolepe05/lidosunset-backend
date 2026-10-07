package lidosunset.service;

import lidosunset.model.dto.AcquistoRistoranteDto;
import lidosunset.model.dto.RigaAcquistoRistoranteDto;
import lidosunset.model.entity.AcquistoRistorante;
import lidosunset.model.entity.Fornitore;
import lidosunset.model.entity.RigaAcquistoRistorante;
import lidosunset.repository.AcquistoRistoranteRepository;
import lidosunset.repository.FornitoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// La classe contiene la logica di business per gli acquisti del ristorante.
@Service
@RequiredArgsConstructor
public class AcquistoRistoranteService {

    private final AcquistoRistoranteRepository acquistoRistoranteRepository;
    private final FornitoreRepository fornitoreRepository;

    // Recupera tutti gli acquisti attivi del ristorante
    @Transactional(readOnly = true)
    public List<AcquistoRistoranteDto> trovaTutti(){
        return acquistoRistoranteRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Cerca un acquisto del ristorante per ID
    @Transactional(readOnly = true)
    public Optional<AcquistoRistoranteDto> trovaPerId(Long id){
        return acquistoRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(this::toDto);
    }

    // Registra un nuovo acquisto per il ristorante e bar
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public AcquistoRistoranteDto salva(AcquistoRistoranteDto dto){
        Fornitore fornitore = fornitoreRepository.findByIdAndAttivaTrue(dto.getFornitoreId())
                .orElseThrow(() -> new IllegalArgumentException("Fornitore non trovato con ID: "+dto.getFornitoreId()));

        AcquistoRistorante nuovoAcquisto = toEntity(dto);
        nuovoAcquisto.setFornitore(fornitore);
        nuovoAcquisto.setAttiva(true);

        // Associa l'acquisto principale alle righe specifiche
        if (nuovoAcquisto.getRighe() != null){
            nuovoAcquisto.getRighe().forEach(riga -> riga.setAcquistoRistorante(nuovoAcquisto));
        }
        return toDto(acquistoRistoranteRepository.save(nuovoAcquisto));
    }

    // Aggiorna un acquisto esistente
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public Optional<AcquistoRistoranteDto> aggiorna(Long id, AcquistoRistoranteDto dto){
        return acquistoRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(esistente -> {
                    // Controllo e aggiornamento del fornitore se è cambiato
                    if (!esistente.getFornitore().getId().equals(dto.getFornitoreId())){
                        Fornitore nuovoFornitore = fornitoreRepository.findByIdAndAttivaTrue(dto.getFornitoreId())
                                .orElseThrow(() -> new IllegalArgumentException("Nuovo fornitore non trovato con ID: "+dto.getFornitoreId()));
                        esistente.setFornitore(nuovoFornitore);
                    }

                    // Aggiornamento dati principali
                    esistente.setDataOrdine(dto.getDataOrdine());
                    esistente.setDataRicezione(dto.getDataRicezione());
                    esistente.setPrezzo(dto.getPrezzoTotale());
                    esistente.setStato(dto.getStato());
                    esistente.setNote(dto.getNote());

                    // Svuota le righe esistenti, o inizializza la lista se era null
                    if (esistente.getRighe() != null){
                        esistente.getRighe().clear();
                    } else {
                        esistente.setRighe(new ArrayList<>());
                    }

                    // Ricostruisce da zero le righe con i dati aggiornati
                    if (dto.getRighe() != null){
                        List<RigaAcquistoRistorante> nuoveRighe = dto.getRighe().stream()
                                .map(this::toRigaEntity)
                                .toList();
                        nuoveRighe.forEach(riga -> {
                            riga.setAcquistoRistorante(esistente);
                            esistente.getRighe().add(riga);
                        });
                    }
                    return toDto(acquistoRistoranteRepository.save(esistente));
                });
    }

    // Elimina l'acquisto di ristorante (in realtà setta attiva=false)
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public boolean elimina(Long id){
        return acquistoRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(acquisto -> {
                    acquisto.setAttiva(false);
                    acquistoRistoranteRepository.save(acquisto);
                    return true;
                }).orElse(false);
    }

    // Converte l'entity in DTO, includendo il nome del fornitore
    private AcquistoRistoranteDto toDto(AcquistoRistorante entity){
        if (entity == null) return null;

        List<RigaAcquistoRistoranteDto> righeDto = entity.getRighe() != null ?
                entity.getRighe().stream().map(this::toRigaDto).toList() : new ArrayList<>();

        return AcquistoRistoranteDto.builder()
                .id(entity.getId())
                .dataOrdine(entity.getDataOrdine())
                .dataRicezione(entity.getDataRicezione())
                .prezzoTotale(entity.getPrezzo())
                .stato(entity.getStato())
                .note(entity.getNote())
                .attiva(entity.isAttiva())
                .fornitoreId(entity.getFornitore().getId())
                .fornitoreNome(entity.getFornitore().getNome())
                .righe(righeDto)
                .build();

    }

    // Converte il DTO in entity per la persistenza
    private AcquistoRistorante toEntity(AcquistoRistoranteDto dto){
        if (dto == null) return null;

        // Lista vuota invece di null se non ci sono righe
        List<RigaAcquistoRistorante> righeEntity = dto.getRighe() != null ?
                new ArrayList<>(dto.getRighe().stream().map(this::toRigaEntity).toList()) : new ArrayList<>();

        return AcquistoRistorante.builder()
                .id(dto.getId())
                .dataOrdine(dto.getDataOrdine())
                .dataRicezione(dto.getDataRicezione())
                .prezzo(dto.getPrezzoTotale())
                .stato(dto.getStato())
                .note(dto.getNote())
                .attiva(dto.isAttiva())
                .righe(righeEntity)
                .build();

    }

    // // Converte una singola riga (entity -> DTO)
    private RigaAcquistoRistoranteDto toRigaDto(RigaAcquistoRistorante entity){
        if (entity == null) return null;
        return RigaAcquistoRistoranteDto.builder()
                .id(entity.getId())
                .prodotto(entity.getProdotto())
                .quantita(entity.getQuantita())
                .prezzoUnitario(entity.getPrezzoUnitario())
                .prezzoTotale(entity.getPrezzoTotale())
                .note(entity.getNote())
                .build();
    }

    // Converte una singola riga (DTO -> entity)
    private RigaAcquistoRistorante toRigaEntity(RigaAcquistoRistoranteDto dto){
        if (dto == null) return null;
        return RigaAcquistoRistorante.builder()
                .id(dto.getId())
                .prodotto(dto.getProdotto())
                .quantita(dto.getQuantita())
                .prezzoUnitario(dto.getPrezzoUnitario())
                .prezzoTotale(dto.getPrezzoTotale())
                .note(dto.getNote())
                .build();
    }
}