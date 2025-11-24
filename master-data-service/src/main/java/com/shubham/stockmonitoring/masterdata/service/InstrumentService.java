package com.shubham.stockmonitoring.masterdata.service;


import com.shubham.stockmonitoring.masterdata.entity.Instrument;
import com.shubham.stockmonitoring.masterdata.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    /**
     * Get all active instruments
     */
    public List<Instrument> getAllActiveInstruments() {
        return instrumentRepository.findByIsActiveTrue();
    }

    /**
     * Get instrument by symbol
     */
    public Optional<Instrument> getInstrumentBySymbol(String symbol) {
        return instrumentRepository.findBySymbol(symbol);
    }

    /**
     * Search instruments by name or symbol
     */
    public List<Instrument> searchInstruments(String query) {
        return instrumentRepository.searchInstruments(query);
    }

    /**
     * Get instruments by exchange
     */
    public List<Instrument> getInstrumentsByExchange(String exchange) {
        return instrumentRepository.findActiveByExchange(exchange);
    }

    /**
     * Get instruments by sector
     */
    public List<Instrument> getInstrumentsBySector(String sector) {
        return instrumentRepository.findBySector(sector);
    }

    /**
     * Add new instrument
     * TODO: Implement Finnhub integration for validation
     */
    @Transactional
    public Instrument addInstrument(Instrument instrument) {
        // TODO: Generate Finnhub symbol format
        // TODO: Verify with Finnhub API
        // TODO: Fetch name from Finnhub if not provided
        
        instrument.setLastVerifiedAt(LocalDateTime.now());

        return instrumentRepository.save(instrument);
    }

    /**
     * Bulk add instruments
     */
    @Transactional
    public void bulkAddInstruments(List<Instrument> instruments) {
        log.info("Bulk adding {} instruments", instruments.size());

        for (Instrument instrument : instruments) {
            try {
                if (!instrumentRepository.existsBySymbol(instrument.getSymbol())) {
                    addInstrument(instrument);
                } else {
                    log.debug("Instrument {} already exists, skipping", instrument.getSymbol());
                }
            } catch (Exception e) {
                log.error("Error adding instrument: {}", instrument.getSymbol(), e);
            }
        }

        log.info("Bulk add completed");
    }

    /**
     * Verify and update instrument validity
     * TODO: Implement Finnhub integration
     */
    @Transactional
    public void verifyInstrument(String symbol) {
        Optional<Instrument> instrumentOpt = instrumentRepository.findBySymbol(symbol);

        if (instrumentOpt.isPresent()) {
            Instrument instrument = instrumentOpt.get();
            // TODO: Verify with Finnhub API
            boolean isValid = true; // Placeholder

            instrument.setIsActive(isValid);
            instrument.setLastVerifiedAt(LocalDateTime.now());

            instrumentRepository.save(instrument);

            log.info("Verified instrument {}: {}", symbol, isValid ? "VALID" : "INVALID");
        }
    }

    /**
     * Refresh all instruments data
     * TODO: Implement Finnhub integration
     */
    @Transactional
    public void refreshAllInstruments() {
        log.info("Starting refresh of all instruments");

        List<Instrument> instruments = instrumentRepository.findAll();

        for (Instrument instrument : instruments) {
            try {
                // TODO: Verify with Finnhub API
                boolean isValid = true; // Placeholder
                instrument.setIsActive(isValid);
                instrument.setLastVerifiedAt(LocalDateTime.now());

                instrumentRepository.save(instrument);

                // Sleep to avoid rate limits
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("Error refreshing instrument: {}", instrument.getSymbol(), e);
            }
        }

        log.info("Finished refreshing all instruments");
    }
}