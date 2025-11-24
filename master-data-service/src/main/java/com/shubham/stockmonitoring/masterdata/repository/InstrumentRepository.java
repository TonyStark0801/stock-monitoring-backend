package com.shubham.stockmonitoring.masterdata.repository;


import com.shubham.stockmonitoring.masterdata.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbol(String symbol);

    Optional<Instrument> findByYahooSymbol(String yahooSymbol);

    List<Instrument> findByExchange(String exchange);

    List<Instrument> findByIsActiveTrue();

    List<Instrument> findBySector(String sector);

    @Query("SELECT i FROM Instrument i WHERE i.isActive = true AND i.exchange = :exchange")
    List<Instrument> findActiveByExchange(String exchange);

    @Query("SELECT i.symbol FROM Instrument i WHERE i.isActive = true")
    List<String> findAllActiveSymbols();

    @Query("SELECT i FROM Instrument i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.symbol) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Instrument> searchInstruments(String query);

    boolean existsBySymbol(String symbol);
}