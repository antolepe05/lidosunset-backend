package lidosunset.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;


// Viene usato CAFFEINE come cache provider al posto del default ConccurrentMapCacheManger
// Usando @EnableCaching si attiva l'AOP di Spring che intercetta le chiamate a metodi annotati
// con @Cacheable, @CacheEvict e @CachePut. Senza questa annotazioni, le annotazioni cache sono ignorate.

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ATTREZZATURA_BY_ID_CACHE = "attrezzaturaById";
    public static final String PRENOTAZIONE_BY_ID_CACHE = "prenotazioneById";
    public static final String CLIENTE_BY_ID_CACHE = "clienteById";
    public static final String DASHBOARD_CACHE = "dashboardCache";

    // Bean che costruisce e configura il gestore della cache, usato da Spring
    // ogni volta che incontra @Cacheable/@CacheEvict/@CachePut nel progetto
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Registra tutti i cache della memoria
        cacheManager.setCacheNames(List.of(
                ATTREZZATURA_BY_ID_CACHE,
                PRENOTAZIONE_BY_ID_CACHE,
                CLIENTE_BY_ID_CACHE,
                DASHBOARD_CACHE
        ));
        cacheManager.setCaffeine(caffeineBuilder());
        return cacheManager;
    }

    // Configura il comportamento di Caffeine: ogni voce scade 10 minuti dopo
    // essere stata scritta, massimo 10000 voci in memoria.
    private Caffeine<Object, Object> caffeineBuilder(){
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .recordStats();
    }
}
