package lidosunset.service;

import lidosunset.config.CacheConfig;
import lidosunset.model.dto.PrenotazioneDto;
import lidosunset.model.entity.Attrezzatura;
import lidosunset.model.entity.Prenotazione;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.StatoPrenotazione;
import lidosunset.repository.AttrezzaturaRepository;
import lidosunset.repository.ClienteRepository;
import lidosunset.repository.PrenotazioneRepository;
import lidosunset.repository.UtenteRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;

// La classe gestisce le prenotazioni sia quelle create dallo staff per conto di un cliente, sia quelle create dal cliente stesso.
// Contiene il calcolo del prezzo, il controllo delle sovrapposizone fra prenotazioni sulla stessa attrezzatura.
@Service
public class PrenotazioneService {
    private final PrenotazioneRepository prenotazioneRepository;
    private final ClienteRepository clienteRepository;
    private final AttrezzaturaRepository attrezzaturaRepository;
    private final UtenteRepository utenteRepository;
    private final CacheManager cacheManager;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository, ClienteRepository clienteRepository, AttrezzaturaRepository attrezzaturaRepository, UtenteRepository utenteRepository, CacheManager cacheManager) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.clienteRepository = clienteRepository;
        this.attrezzaturaRepository = attrezzaturaRepository;
        this.utenteRepository = utenteRepository;
        this.cacheManager = cacheManager;
    }

    // Trova tutte le prenotazioni
    @Transactional(readOnly = true)
    public List<PrenotazioneDto> trovaTutte(){
        return prenotazioneRepository.findByAttivaTrue().stream()
                .map(this::toDto)
                .toList();
    }

    // Trova le prenotazioni per ID
    @Cacheable(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<PrenotazioneDto> trovaPerId(Long id){
        return prenotazioneRepository.findByIdAndAttivaTrue(id).map(this::toDto);
    }

    // Trova le prenotazioni di un cliente partendo dallo username/email di login.
    // Il subject del JWT è sempre lo USERNAME, quindi prima risolviamo l'email
    // reale dell'utente tramite UtenteRepository, poi cerchiamo il Cliente con quella email.
    @Transactional(readOnly = true)
    public List<PrenotazioneDto> trovaPerEmailCliente(String usernameOrEmail){
        String emailReale = risolviEmailReale(usernameOrEmail);
        return clienteRepository.findByEmailAndAttivaTrue(emailReale)
                .map(cliente -> prenotazioneRepository.findByClienteIdAndAttivaTrue(cliente.getId()).stream()
                        .map(this::toDto)
                        .collect(toList()))
                .orElse(List.of());
    }

    // Crea o aggiorna una prenotazione
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#dto.id", condition = "#dto.id != null"),
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#dto.attrezzaturaId", condition = "#dto.attrezzaturaId != null"),
            @CacheEvict(value = "dashboardCache", allEntries = true)
    })
    @Transactional
    public PrenotazioneDto salva(PrenotazioneDto dto){
        if (dto.getAttrezzaturaId() != null){
            // Controlla che l'attrezzatura non sia già prenotata nella stessa fascia oraria
            boolean giaOccupata = prenotazioneRepository.isAttrezzaturaOccupata(
                    dto.getAttrezzaturaId(),
                    dto.getDataInizio(),
                    dto.getOraInizio(),
                    dto.getOraFine(),
                    dto.getId()
            );
            if (giaOccupata){
                throw new IllegalStateException("Attrezzatura già prenotata in questa fascia oraria.");
            }
        }

        Prenotazione entity;

        // Se il DTO ha già un id, è una modifica di una prenotazione esistente
        if (dto.getId() != null){
            entity = prenotazioneRepository.findByIdAndAttivaTrue(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata con ID: " + dto.getId()));

            Attrezzatura vecchiaAttrezzatura = entity.getAttrezzatura();

            entity.setData(dto.getDataInizio());
            entity.setOraInizio(dto.getOraInizio());
            entity.setOraFine(dto.getOraFine());
            entity.setStato(dto.getStato());

            if (dto.getClienteId() != null){
                entity.setCliente(clienteRepository.findByIdAndAttivaTrue(dto.getClienteId())
                        .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato o non attivo con ID: " + dto.getClienteId())));
            }

            if (dto.getAttrezzaturaId() != null){
                Attrezzatura nuovaAttrezzatura = attrezzaturaRepository.findByIdAndAttivaTrue(dto.getAttrezzaturaId())
                        .orElseThrow(() -> new IllegalArgumentException("Attrezzatura non trovata o non attiva con ID: " + dto.getAttrezzaturaId()));

                // True se l'attrezzatura è diversa da quella già associata (o se
                // prima non ce n'era nessuna) - serve per non bloccare per errore
                // l'aggiornamento di una prenotazione che mantiene la stessa
                // attrezzatura, che si trova in PRENOTATA proprio a causa sua
                boolean attrezzaturaCambiata = vecchiaAttrezzatura == null
                        || !vecchiaAttrezzatura.getId().equals(nuovaAttrezzatura.getId());

                // Blocca solo se l'attrezzatura sta davvero cambiando: isAttrezzaturaOccupata
                // sopra controlla solo sovrapposizioni con altre prenotazioni, non
                // lo stato dell'attrezzatura (es. IN_MANUTENZIONE) - senza questo
                // controllo si potrebbe prenotare un'attrezzatura non disponibile
                if (attrezzaturaCambiata && entity.getStato() == StatoPrenotazione.ATTIVA
                        && nuovaAttrezzatura.getStato() != StatoAttrezzatura.DISPONIBILE){
                    throw new IllegalStateException("L'attrezzatura non è disponibile (stato attuale: " + nuovaAttrezzatura.getStato() + ").");
                }

                // Se l'attrezzatura sta cambiando, quella vecchia torna disponibile e va invalidata la sua cache id
                if (attrezzaturaCambiata && vecchiaAttrezzatura != null){
                    vecchiaAttrezzatura.setStato(StatoAttrezzatura.DISPONIBILE);
                    attrezzaturaRepository.save(vecchiaAttrezzatura);
                    svuotaCacheIncrociata(entity.getId(), vecchiaAttrezzatura.getId());
                }
                entity.setAttrezzatura(nuovaAttrezzatura);
            }

            if (entity.getAttrezzatura() != null){
                entity.setPrezzo(calcolaPrezzo(entity.getAttrezzatura(), entity.getOraInizio(), entity.getOraFine()));
            }

        } else {
            // Altrimenti è una nuova prenotazione da creare da zero
            entity = toEntity(dto);
            entity.setAttiva(true);
            if (entity.getStato() == null){
                entity.setStato(StatoPrenotazione.ATTIVA);
            }

            if (entity.getStato() == StatoPrenotazione.ATTIVA){
                LocalDateTime inizioRichiesto = LocalDateTime.of(entity.getData(), entity.getOraInizio());
                if (inizioRichiesto.isBefore(LocalDateTime.now())){
                    throw new IllegalStateException("Non puoi creare una prenotazione attiva per una data/ora già passata.");
                }
                // Stesso controllo di sopra, applicato alla creazione: una nuova
                // prenotazione attiva richiede un'attrezzatura effettivamente disponibile
                if (entity.getAttrezzatura() != null && entity.getAttrezzatura().getStato() != StatoAttrezzatura.DISPONIBILE){
                    throw new IllegalStateException("L'attrezzatura non è disponibile (stato attuale: " + entity.getAttrezzatura().getStato() + ").");
                }
            }

            if (entity.getAttrezzatura() != null){
                entity.setPrezzo(calcolaPrezzo(entity.getAttrezzatura(), entity.getOraInizio(), entity.getOraFine()));
            }
        }

        // Se la prenotazione risultante è attiva, l'attrezzatura passa a prenotata
        if (entity.getAttrezzatura() != null && entity.getStato() == StatoPrenotazione.ATTIVA){
            entity.getAttrezzatura().setStato(StatoAttrezzatura.PRENOTATA);
            attrezzaturaRepository.save(entity.getAttrezzatura());
        }
        Prenotazione salvata = prenotazioneRepository.save(entity);
        return toDto(salvata);
    }

    // Annulla prenotazione
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public void annulla(Long id){
        prenotazioneRepository.findByIdAndAttivaTrue(id).ifPresent(prenotazione -> {
            prenotazione.setStato(StatoPrenotazione.ANNULLATA);
            prenotazione.setAttiva(false);
            prenotazioneRepository.save(prenotazione);

            // Quando l'attrezzatura viene liberata, torna nello stato DISPONIBILE
            if (prenotazione.getAttrezzatura() != null){
                prenotazione.getAttrezzatura().setStato(StatoAttrezzatura.DISPONIBILE);
                attrezzaturaRepository.save(prenotazione.getAttrezzatura());
                svuotaCacheIncrociata(prenotazione.getId(), prenotazione.getAttrezzatura().getId());
            }
        });
    }

    // Restituisce il dettaglio di una prenotazione solo se appartiene al cliente loggato
    @Cacheable(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<PrenotazioneDto> trovaMiaPerId(Long id, String usernameOrEmail){
        String emailReale = risolviEmailReale(usernameOrEmail);
        return prenotazioneRepository.findByIdAndAttivaTrue(id)
                .filter(p -> p.getCliente().getEmail().equalsIgnoreCase(emailReale))
                .map(this::toDto);
    }

    // Crea una prenotazione forzando la proprietà al cliente attualmente loggato
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#dto.attrezzaturaId", condition = "#dto.attrezzaturaId != null"),
            @CacheEvict(value = "dashboardCache", allEntries = true)
    })
    @Transactional
    public PrenotazioneDto salvaMia(PrenotazioneDto dto, String usernameOrEmail) {
        String emailReale = risolviEmailReale(usernameOrEmail);
        return clienteRepository.findByEmailAndAttivaTrue(emailReale).map(cliente -> {
            dto.setClienteId(cliente.getId());
            return salva(dto);
        }).orElseThrow(() -> new RuntimeException("Impossibile creare la prenotazione: Cliente non trovato per l'email " + emailReale));
    }

    // Modifica la prenotazione del cliente loggato
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#dto.attrezzaturaId", condition = "#dto.attrezzaturaId != null"),
            @CacheEvict(value = "dashboardCache", allEntries = true)
    })
    @Transactional
    public Optional<PrenotazioneDto> aggiornaMia(Long id, PrenotazioneDto dto, String usernameOrEmail) {
        String emailReale = risolviEmailReale(usernameOrEmail);
        return prenotazioneRepository.findByIdAndAttivaTrue(id)
                .filter(p -> p.getCliente().getEmail().equalsIgnoreCase(emailReale))
                .map(esistente -> {
                    dto.setId(id);
                    dto.setClienteId(esistente.getCliente().getId());
                    return salva(dto);
                });
    }

    // Annulla la prenotazione del cliente loggato
    @CacheEvict(value = "dashboardCache", allEntries = true)
    @Transactional
    public boolean annullaMia(Long id, String usernameOrEmail) {
        String emailReale = risolviEmailReale(usernameOrEmail);
        return prenotazioneRepository.findByIdAndAttivaTrue(id)
                .filter(p -> p.getCliente().getEmail().equalsIgnoreCase(emailReale))
                .map(prenotazione -> {
                    prenotazione.setStato(StatoPrenotazione.ANNULLATA);
                    prenotazione.setAttiva(false);
                    prenotazioneRepository.save(prenotazione);

                    if (prenotazione.getAttrezzatura() != null){
                        prenotazione.getAttrezzatura().setStato(StatoAttrezzatura.DISPONIBILE);
                        attrezzaturaRepository.save(prenotazione.getAttrezzatura());
                        svuotaCacheIncrociata(prenotazione.getId(), prenotazione.getAttrezzatura().getId());
                    }
                    return true;
                }).orElse(false);
    }

    // Il subject del JWT (quindi Principal.getName()) è sempre lo username di
    // login, non necessariamente l'email del cliente in anagrafica. Risolviamo
    // qui l'email reale dell'utente, da usare per cercare il Cliente corrispondente.
    private String risolviEmailReale(String usernameOrEmail){
        return utenteRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(lidosunset.model.entity.Utente::getEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + usernameOrEmail));
    }

    // Calcola il prezzo a "scatti" di 10 minuti, con tariffa diversa per tipo di attrezzatura
    private BigDecimal calcolaPrezzo(Attrezzatura attrezzatura, LocalTime oraInizio, LocalTime oraFine) {
        long minutiTotali = Duration.between(oraInizio, oraFine).toMinutes();

        if (minutiTotali % 10 != 0)
            throw new IllegalStateException("Errore interno: la durata deve essere un multiplo di 10 minuti.");

        long scatti = minutiTotali / 10;

        if (attrezzatura.getTipo() == null)
            throw new IllegalStateException("L'attrezzatura selezionata non ha un tipo definito per il calcolo del prezzo.");

        BigDecimal tariffaPer10Min = switch (attrezzatura.getTipo()) {
            case CANOA, PEDALO -> BigDecimal.valueOf(20.0);
            case JETSKI -> BigDecimal.valueOf(40.0);
        };
        return tariffaPer10Min.multiply(BigDecimal.valueOf(scatti));
    }

    // Svuota tramite il CacheManager la cache per-id sia della prenotazione che delle attrezzature coinvolte
    // e viene utilizzato ogni volta che l'attrezzatura associata a una prenotazione cambia stato o viene liberata
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRENOTAZIONE_BY_ID_CACHE, key = "#idPrenotazione"),
            @CacheEvict(value = CacheConfig.ATTREZZATURA_BY_ID_CACHE, key = "#idAttrezzatura"),
    })
    public void svuotaCacheIncrociata(Long idPrenotazione, Long idAttrezzatura) {
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
    private PrenotazioneDto toDto(Prenotazione entity){
        if (entity == null) return null;

        PrenotazioneDto.PrenotazioneDtoBuilder builder = PrenotazioneDto.builder()
                .id(entity.getId())
                .dataInizio(entity.getData())
                .oraInizio(entity.getOraInizio())
                .oraFine(entity.getOraFine())
                .stato(entity.getStato())
                .attiva(entity.isAttiva())
                .prezzo(entity.getPrezzo());

        if (entity.getCliente() != null){
            builder.clienteId(entity.getCliente().getId())
                    .nome(entity.getCliente().getNome())
                    .cognome(entity.getCliente().getCognome());
        }

        if (entity.getAttrezzatura() != null){
            builder.attrezzaturaId(entity.getAttrezzatura().getId())
                    .attrezzaturaNome(entity.getAttrezzatura().getNome());
        }
        return builder.build();
    }

    // Converte il DTO in entity per la persistenza
    private Prenotazione toEntity(PrenotazioneDto dto){
        if (dto == null) return null;

        Prenotazione entity = new Prenotazione();
        entity.setId(dto.getId());
        entity.setData(dto.getDataInizio());
        entity.setOraInizio(dto.getOraInizio());
        entity.setOraFine(dto.getOraFine());
        entity.setStato(dto.getStato());
        entity.setAttiva(dto.isAttiva());

        if (dto.getClienteId() != null){
            entity.setCliente(clienteRepository.findByIdAndAttivaTrue(dto.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato o non attivo con ID: " + dto.getClienteId())));
        }

        if (dto.getAttrezzaturaId() != null){
            entity.setAttrezzatura(attrezzaturaRepository.findByIdAndAttivaTrue(dto.getAttrezzaturaId())
                    .orElseThrow(() -> new IllegalArgumentException("Attrezzatura non trovata o non attiva con ID: " + dto.getAttrezzaturaId())));
        }
        return entity;
    }
}