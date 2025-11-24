package com.shubham.stockmonitoring.masterdata.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private String symbol;
    private String name;
    private String description;
    private String sector;
    private String exchange;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private Long volume;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
