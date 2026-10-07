package lidosunset.config;

import lidosunset.model.entity.*;
import lidosunset.repository.AttrezzaturaRepository;
import lidosunset.repository.UtenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDatabase(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, AttrezzaturaRepository attrezzaturaRepository){
        return args -> {
            if (!utenteRepository.existsByUsername("admin")) {
                utenteRepository.save(Utente.builder()
                        .username("admin")
                        .email("admin@lidosunset.it")
                        .password(passwordEncoder.encode("admin"))
                        .role(Role.ADMIN)
                        .build()
                );
            }

            if (!utenteRepository.existsByUsername("istruttore")) {
                utenteRepository.save(Utente.builder()
                        .username("istruttore")
                        .email("istruttore@lidosunset2026.it")
                        .password(passwordEncoder.encode("istruttore2026"))
                        .role(Role.ISTRUTTORE)
                        .build()
                );
            }

            if (!utenteRepository.existsByUsername("barista")) {
                utenteRepository.save(Utente.builder()
                        .username("barista")
                        .email("barista@lidosunset2026.it")
                        .password(passwordEncoder.encode("barista2026"))
                        .role(Role.BARISTA)
                        .build()
                );
            }

            if (!utenteRepository.existsByUsername("cliente")) {
                utenteRepository.save(Utente.builder()
                        .username("cliente")
                        .email("cliente@lidosunset2026.it")
                        .password(passwordEncoder.encode("clienteLidoSunset"))
                        .role(Role.CLIENTE)
                        .build()
                );
            }

            if (attrezzaturaRepository.count() == 0) {
                List<Attrezzatura> parcoIniziale = List.of(
                        // CANOE
                        Attrezzatura.builder()
                                .nome("Canoa Oceanica Singola Gialla")
                                .tipo(TipoAttrezzatura.CANOA)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build(),

                        Attrezzatura.builder()
                                .nome("Canoa Explorer Doppia Verde")
                                .tipo(TipoAttrezzatura.CANOA)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build(),

                        // PEDALÒ
                        Attrezzatura.builder()
                                .nome("Pedalò Standard 4 Posti Blu")
                                .tipo(TipoAttrezzatura.PEDALO)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build(),
                        Attrezzatura.builder()
                                .nome("Pedalò Sprint con Scivolo Rosso")
                                .tipo(TipoAttrezzatura.PEDALO)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build(),

                        // JETSKI
                        Attrezzatura.builder()
                                .nome("JetSki Sea-Doo Spark 90hp")
                                .tipo(TipoAttrezzatura.JETSKI)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build(),
                        Attrezzatura.builder()
                                .nome("JetSki Yamaha WaveRunner VX")
                                .tipo(TipoAttrezzatura.JETSKI)
                                .stato(StatoAttrezzatura.DISPONIBILE)
                                .attiva(true)
                                .build()
                );

                attrezzaturaRepository.saveAll(parcoIniziale);
                System.out.println(">> [LIDO SUNSET] Inventario nautico inizializzato con " + parcoIniziale.size() + " mezzi pronti al noleggio.");
            } else {
                System.out.println(">> [LIDO SUNSET] Dati già presenti nel database. Skip del seeding.");
            }
        };
    }
}