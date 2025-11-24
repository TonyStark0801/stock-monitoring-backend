package com.shubham.stockmonitoring.masterdata.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndexResponse {
    private String symbol;           // e.g., "^NSEI" for Nifty 50
    private String name;              // "Nifty 50"
    private BigDecimal currentPrice;
    private BigDecimal change;       // Absolute change
    private BigDecimal changePercent; // Percentage change
    private BigDecimal previousClose;
    private Long volume;
    private String exchange;          // "NSE" or "BSE"
}