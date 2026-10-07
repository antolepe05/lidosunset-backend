package lidosunset.service;

import lidosunset.config.CacheConfig;
import lidosunset.model.dto.ClienteDto;
import lidosunset.model.entity.Cliente;
import lidosunset.repository.ClienteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// La classe gestisce l'anagrafica dei clienti: ricerche, paginazione, salvataggio  ed eliminazione
@Service
public class ClienteService {
    private final ClienteRepository  clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Trova tutti i clienti
    @Transactional(readOnly = true)
    public List<ClienteDto> trovaTutti(){
        return clienteRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Trova paginati
    @Transactional(readOnly = true)
    public Page<ClienteDto> trovaPaginati(Pageable pageable){
        return clienteRepository.findByAttivaTrue(pageable).map(this::toDto);
    }

    // Trova il cliente per ID
    @Cacheable(value = CacheConfig.CLIENTE_BY_ID_CACHE, key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<ClienteDto> trovaPerId(Long id){
        return clienteRepository.findByIdAndAttivaTrue(id).map(this::toDto);
    }

    // Salva (o aggiorna) il cliente. Riceve DTO, converte in Entity, salva e risponde in DTO
    @CacheEvict(value = CacheConfig.CLIENTE_BY_ID_CACHE, key = "#dto.id", condition = "#dto.id != null")
    @Transactional
    public ClienteDto salva(ClienteDto dto){
        Cliente entity;
        // Se il DTO ha già un id, è una modifica del cliente esistente;
        // altrimenti è la creazione di un nuovo cliente da zero
        if (dto.getId() != null){
            entity = clienteRepository.findByIdAndAttivaTrue(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato con ID: "+dto.getId()));

            entity.setNome(dto.getNome());
            entity.setCognome(dto.getCognome());
            entity.setEmail(dto.getEmail());
            entity.setTelefono(dto.getTelefono());
        } else {
            entity = toEntity(dto);
            entity.setAttiva(true);
        }

        Cliente salvato = clienteRepository.save(entity);
        return toDto(salvato);
    }

    // Elimina il cliente per ID (in realtà non viene eliminato ma viene disattivato)
    @CacheEvict(value = CacheConfig.CLIENTE_BY_ID_CACHE, key = "#id")
    @Transactional
    public void elimina(Long id) {
        clienteRepository.findById(id).ifPresent(cliente -> {
            cliente.setAttiva(false);
            clienteRepository.save(cliente);
        });
    }

    // Cerca il cliente per cognome
    @Transactional(readOnly = true)
    public List<ClienteDto> trovaByCognome(String cognome){
        return clienteRepository.findByCognomeAndAttivaTrue(cognome).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Ricerca per nome o cognome
    @Transactional(readOnly = true)
    public List<ClienteDto> cercaPerNomeOCognome(String query){
        return clienteRepository.cercaPerNomeOCognome(query).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Trova un cliente partendo dalla sua email
    @Transactional(readOnly = true)
    public Optional<ClienteDto> trovaPerEmail(String email) {
        return clienteRepository.findByEmailAndAttivaTrue(email)
                .map(this::toDto);
    }


    // Metodi di conversione

    // Converte l'entity in DTo
    private ClienteDto toDto(Cliente entity){
        ClienteDto dto = new ClienteDto();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setCognome(entity.getCognome());
        dto.setEmail(entity.getEmail());
        dto.setTelefono(entity.getTelefono());
        dto.setAttiva(entity.isAttiva());
        return dto;
    }

    // Converte il DTO in entity per la persistenza
    private Cliente toEntity(ClienteDto dto){
        Cliente entity = new Cliente();
        entity.setId(dto.getId());
        entity.setNome(dto.getNome());
        entity.setCognome(dto.getCognome());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
        entity.setAttiva(dto.isAttiva());
        return entity;
    }
}
