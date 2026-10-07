package lidosunset.service;

import lidosunset.model.dto.VenditaRistoranteDto;
import lidosunset.model.entity.VenditaRistorante;
import lidosunset.repository.VenditaRistoranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// La classe gestisce le vendite del ristorante (incasso).

@Service
@RequiredArgsConstructor
public class VenditaRistoranteService {
    private final VenditaRistoranteRepository venditaRistoranteRepository;

    // Recupera tutte le vendite attive
    @Transactional(readOnly = true)
    public List<VenditaRistoranteDto> trovaTutte(){
        return venditaRistoranteRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Cerca una vendita per ID
    @Transactional(readOnly = true)
    public Optional<VenditaRistoranteDto> trovaPerId(Long id){
        return venditaRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(this::toDto);
    }

    // Registra una nuova vendita
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public VenditaRistoranteDto salva(VenditaRistoranteDto dto){
        VenditaRistorante nuovaVendita = VenditaRistorante.builder()
                .data(dto.getData())
                .totale(dto.getTotale())
                .note(dto.getNote())
                .attiva(true)
                .build();

        return toDto(venditaRistoranteRepository.save(nuovaVendita));
    }

    // Aggiorna una vendita esistente
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public Optional<VenditaRistoranteDto> aggiorna(Long id, VenditaRistoranteDto dto){
        return venditaRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(esistente -> {
                    esistente.setData(dto.getData());
                    esistente.setTotale(dto.getTotale());
                    esistente.setNote(dto.getNote());
                    return toDto(venditaRistoranteRepository.save(esistente));
                });
    }

    // Elimina una vendita (in realtà setta attiva=false)
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public boolean elimina(Long id){
        return venditaRistoranteRepository.findByIdAndAttivaTrue(id)
                .map(vendita -> {
                    vendita.setAttiva(false);
                    venditaRistoranteRepository.save(vendita);
                    return true;
                }).orElse(false);
    }

    // Converte l'entity in DTO
    private VenditaRistoranteDto toDto(VenditaRistorante entity){
        if (entity == null) return null;
        return VenditaRistoranteDto.builder()
                .id(entity.getId())
                .data(entity.getData())
                .totale(entity.getTotale())
                .note(entity.getNote())
                .attiva(entity.isAttiva())
                .build();
    }
}