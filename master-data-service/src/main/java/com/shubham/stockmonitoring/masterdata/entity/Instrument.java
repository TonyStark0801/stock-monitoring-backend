package com.shubham.stockmonitoring.masterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "instruments", indexes = {
        @Index(name = "idx_symbol", columnList = "symbol"),
        @Index(name = "idx_exchange", columnList = "exchange"),
        @Index(name = "idx_active", columnList = "is_active")
},
        schema = "masterdata"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String symbol;  // e.g., "RELIANCE"

    @Column(nullable = false, length = 200)
    private String name;  // e.g., "Reliance Industries Ltd"

    @Column(nullable = false, length = 10)
    private String exchange;  // "NSE" or "BSE"

    @Column(name = "yahoo_symbol", length = 50)
    private String yahooSymbol;  // e.g., "RELIANCE.NS"

    @Column(length = 50)
    private String sector;  // e.g., "Oil & Gas"

    @Column(length = 100)
    private String industry;  // e.g., "Refineries"

    @Column(name = "isin", length = 20)
    private String isin;  // International Securities Identification Number

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}