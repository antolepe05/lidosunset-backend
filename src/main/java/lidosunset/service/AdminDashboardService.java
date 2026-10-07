package lidosunset.service;

import lidosunset.model.dto.DashboardDatiDto;
import lidosunset.model.entity.Prenotazione;
import lidosunset.model.entity.StatoAcquisto;
import lidosunset.model.entity.StatoAttrezzatura;
import lidosunset.model.entity.StatoPrenotazione;
import lidosunset.model.entity.TipoManutenzione;
import lidosunset.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Calcola tutti gli indicatori chiave di prestazione della dashboard admin in un solo metodo,
// leggendo sei repository diversi e aggregando con gli stream di Java.
// È poco efficiente perché carica tutte le righe attive in memoria e poi fa la somma in Java.
// L'alternativa sarebbe stata quella di usare delle aggregazioni a livello di DB

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final PrenotazioneRepository prenotazioneRepository;
    private final VenditaRistoranteRepository venditaRistoranteRepository;
    private final AcquistoRistoranteRepository acquistoRistoranteRepository;
    private final AcquistoAttrezzaturaRepository acquistoAttrezzaturaRepository;
    private final AttrezzaturaRepository attrezzaturaRepository;
    private final ManutenzioneRepository manutenzioneRepository;

    // La chiave di cache è una stringa fissa ("report_giornaliero") perché il metodo non riceve parametri.
    // Quando qualcosa cambia (nuova prenotazione, nuovo acquisto, ecc..), quella casella viene svuotata
    // così la prossima volta i dati vengono ricalcolati da capo invece di restare vecchi
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardCache", key = "'report_giornaliero'")
    public DashboardDatiDto generaReportGiornaliero() {

        // Incasso delle prenotazioni (somma i prezzi delle prenotazioni completate)
        BigDecimal incassoPrenotazioni = prenotazioneRepository.findByAttivaTrue().stream()
                .filter(p -> p.getStato() == StatoPrenotazione.COMPLETATA)
                .map(Prenotazione::getPrezzo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Incasso ristorante: Somma i ricavi totali delle vendite del bar/ristorante
        BigDecimal incassoRistorante = venditaRistoranteRepository.findByAttivaTrue().stream()
                .map( v -> v.getTotale())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Spese ristorante: somma i costi degli ordini EFFETTIVAMENTE RICEVUTI.
        // Gli ordini ancora IN_ATTESA o ANNULLATI non rappresentano una spesa reale.
        BigDecimal speseRistorante = acquistoRistoranteRepository.findByAttivaTrue().stream()
                .filter(a -> a.getStato() == StatoAcquisto.RICEVUTO)
                .map(a -> a.getPrezzo())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Spese attrezzature: solo gli ordini RICEVUTO contano come spesa reale.
        // Il campo prezzoTotale è Double, va convertito in BigDecimal per la somma.
        BigDecimal speseAttrezzature = acquistoAttrezzaturaRepository.findByAttivaTrue().stream()
                .filter(a -> a.getStato() == StatoAcquisto.RICEVUTO)
                .map(a -> a.getPrezzoTotale())
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Spese manutenzione: somma i costi sostenuti per riparare le attrezzature
        BigDecimal speseManutenzione = manutenzioneRepository.findByAttivaTrue().stream()
                .map(m -> m.getCosto())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcolo margine netto
        BigDecimal entrate = incassoPrenotazioni.add(incassoRistorante);
        BigDecimal uscite = speseManutenzione.add(speseRistorante).add(speseAttrezzature);
        BigDecimal margineNetto = entrate.subtract(uscite);

        // Conteggio attrezzature totali e suddivise per stato
        Map<String, Long> statiMappa = new HashMap<>();
        for (StatoAttrezzatura stato : StatoAttrezzatura.values()){
            long count = attrezzaturaRepository.findByStatoAndAttivaTrue(stato).size();
            statiMappa.put(stato.name(), count);
        }
        long totaleMezzi = attrezzaturaRepository.findByAttivaTrue().size();

        // Quante manutenzioni urgenti sono ancora non completate
        long urgenzeAperte = manutenzioneRepository.findByAttivaTrue().stream()
                .filter(m -> m.getTipo() == TipoManutenzione.URGENTE && !m.isCompletata())
                .count();

        return DashboardDatiDto.builder()
                .incassoTotalePrenotazioni(incassoPrenotazioni)
                .incassoTotaleRistorante(incassoRistorante)
                .totaleSpeseRistorante(speseRistorante)
                .totaleSpeseAttrezzature(speseAttrezzature)
                .totaleSpeseManutenzione(speseManutenzione)
                .margineNetto(margineNetto)
                .totaleAttrezzatura(totaleMezzi)
                .contatoriPerStato(statiMappa)
                .manutenzioniUrgentiAttive(urgenzeAperte)
                .build();
    }
}