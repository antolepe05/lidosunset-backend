package lidosunset.service;


import lidosunset.model.dto.FornitoreDto;
import lidosunset.model.entity.Fornitore;
import lidosunset.repository.FornitoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// La classe gestisce l'anagrafica fornitori, con controllo di unicità su P.IVA ed
// email alla creazione.

@Service
@RequiredArgsConstructor
public class FornitoreService {
    private final FornitoreRepository fornitoreRepository;

    // Recupera tutti i fornitori
    @Transactional(readOnly = true)
    public List<FornitoreDto> trovaTutti(){
        return fornitoreRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Cerca un singolo fornitore tramite ID
    @Transactional(readOnly = true)
    public Optional<FornitoreDto> trovaPerId(Long id){
        return fornitoreRepository.findByIdAndAttivaTrue(id)
                .map(this::toDto);
    }

    // Salva un nuovo fornitore
    @Transactional
    public FornitoreDto salva(FornitoreDto dto){
        // Non possono esistere fornitore con la stessa P.IVA
        if (fornitoreRepository.findByPartitaIva(dto.getPartitaIva()).isPresent()){
            throw new IllegalArgumentException("Un fornitore con questa PARTITA IVA è già presente");
        }
        // Non esistono fornitori con la stessa email
        if (fornitoreRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("Un fornitore con questa email esiste già");
        }
        Fornitore nuovoFornitore = toEntity(dto);
        // In stato di creazione forza il parametro attiva a true
        nuovoFornitore.setAttiva(true);

        return toDto(fornitoreRepository.save(nuovoFornitore));
    }

    // Aggiorna i dati di un fornitore esistente
    @Transactional
    public Optional<FornitoreDto> aggiorna(Long id, FornitoreDto dto){
        return fornitoreRepository.findByIdAndAttivaTrue(id)
                .map(esistente -> {
                    esistente.setNome(dto.getNome());
                    esistente.setPartitaIva(dto.getPartitaIva());
                    esistente.setEmail(dto.getEmail());
                    esistente.setTelefono(dto.getTelefono());
                    esistente.setIndirizzo(dto.getIndirizzo());
                    esistente.setSettore(dto.getSettore());

                    return toDto(fornitoreRepository.save(esistente));
                });
    }

    // Elmina il fornitore (in realtà non elimina il fornitore ma setta il parametro attiva=false)
    @Transactional
    public boolean elimina(Long id){
        return fornitoreRepository.findByIdAndAttivaTrue(id)
                .map(fornitore -> {
                    fornitore.setAttiva(false);
                    fornitoreRepository.save(fornitore);
                    return true;
                })
                .orElse(false);
    }

    private FornitoreDto toDto(Fornitore entity){
        if (entity == null) return null;

        return FornitoreDto.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .partitaIva(entity.getPartitaIva())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .indirizzo(entity.getIndirizzo())
                .settore(entity.getSettore())
                .attiva(entity.isAttiva())
                .build();
    }

    private Fornitore toEntity(FornitoreDto dto){
        if (dto == null) return null;

        return Fornitore.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .partitaIva(dto.getPartitaIva())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .indirizzo(dto.getIndirizzo())
                .settore(dto.getSettore())
                .attiva(dto.isAttiva())
                .build();
    }
}
