package com.shubham.stockmonitoring.masterdata.controller;

import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.MarketIndexResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.MarketSummaryResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.StockPriceResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.TrendingStockResponse;
import com.shubham.stockmonitoring.masterdata.service.MarketService;
import com.shubham.stockmonitoring.masterdata.service.StockDataSeederService;
import com.shubham.stockmonitoring.masterdata.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final MarketService marketService;
    private final StockDataSeederService stockDataSeederService;

    @GetMapping()
    public BaseResponse getAllStocks(Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    /**
     * Get stocks with real-time prices
     */
    @GetMapping("/prices")
    public BaseResponse getStocksWithPrices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Fetching stocks with prices - page: {}, size: {}", page, size);
            List<StockPriceResponse> stocks = marketService.getStocksWithPrices(page, size);
            log.info("Successfully fetched {} stocks", stocks.size());
            return BaseResponse.success(stocks);
        } catch (Exception e) {
            log.error("Error fetching stocks with prices", e);
            return BaseResponse.error("Failed to fetch stocks: " + e.getMessage());
        }
    }

    /**
     * Get market indices (Nifty, Sensex, etc.)
     */
    @GetMapping("/indices")
    public BaseResponse getMarketIndices() {
        try {
            log.info("Fetching market indices");
            List<MarketIndexResponse> indices = marketService.getMarketIndices();
            log.info("Successfully fetched {} indices", indices.size());
            return BaseResponse.success(indices);
        } catch (Exception e) {
            log.error("Error fetching market indices", e);
            return BaseResponse.error("Failed to fetch market indices: " + e.getMessage());
        }
    }

    /**
     * Get trending stocks
     */
    @GetMapping("/trending")
    public BaseResponse getTrendingStocks(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Fetching trending stocks - limit: {}", limit);
            List<TrendingStockResponse> trending = marketService.getTrendingStocks(limit);
            log.info("Successfully fetched {} trending stocks", trending.size());
            return BaseResponse.success(trending);
        } catch (Exception e) {
            log.error("Error fetching trending stocks", e);
            return BaseResponse.error("Failed to fetch trending stocks: " + e.getMessage());
        }
    }

    /**
     * Get market summary (indices + trending stocks)
     */
    @GetMapping("/market-summary")
    public BaseResponse getMarketSummary() {
        try {
            log.info("Fetching market summary");
            MarketSummaryResponse summary = marketService.getMarketSummary();
            log.info("Successfully fetched market summary");
            return BaseResponse.success(summary);
        } catch (Exception e) {
            log.error("Error fetching market summary", e);
            return BaseResponse.error("Failed to fetch market summary: " + e.getMessage());
        }
    }

    /**
     * Seed US stocks for Finnhub free tier
     * This endpoint populates the database with popular US stocks
     */
    @PostMapping("/seed")
    public BaseResponse seedUSStocks() {
        try {
            log.info("Starting to seed US stocks");
            stockDataSeederService.seedUSStocks();
            log.info("Successfully seeded US stocks");
            return BaseResponse.success("US stocks seeded successfully");
        } catch (Exception e) {
            log.error("Error seeding US stocks", e);
            return BaseResponse.error("Failed to seed stocks: " + e.getMessage());
        }
    }
}
