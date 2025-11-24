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
public class TrendingStockResponse {
    private String symbol;
    private String name;
    private String exchange;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private Long volume;
    private String trendReason; // "HIGH_VOLUME", "PRICE_SURGE", "TOP_GAINER", etc.
}