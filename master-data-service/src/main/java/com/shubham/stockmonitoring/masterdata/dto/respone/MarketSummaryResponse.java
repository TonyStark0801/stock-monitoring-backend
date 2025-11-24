package com.shubham.stockmonitoring.masterdata.dto.respone;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSummaryResponse {
    private List<MarketIndexResponse> indices;
    private List<TrendingStockResponse> trendingStocks;
    private Long totalActiveStocks;
    private String marketStatus; // "OPEN", "CLOSED", "PRE_MARKET", "POST_MARKET"
}