package com.shubham.stockmonitoring.masterdata.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks", schema = "masterdata")
@Data
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    private String sector;
    private String exchange;
    
    @Column(name = "current_price")
    private BigDecimal currentPrice;
    
    @Column(name = "market_cap")
    private BigDecimal marketCap;
    
    @Column(name = "volume")
    private Long volume;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "created_at")
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
