package com.shubham.stockmonitoring.masterdata.service;

import com.shubham.stockmonitoring.masterdata.dto.finnhub.FinnhubCompanyProfileResponse;
import com.shubham.stockmonitoring.masterdata.dto.finnhub.FinnhubQuoteResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.MarketIndexResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.MarketSummaryResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.StockPriceResponse;
import com.shubham.stockmonitoring.masterdata.dto.respone.TrendingStockResponse;
import com.shubham.stockmonitoring.masterdata.entity.Instrument;
import com.shubham.stockmonitoring.masterdata.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

    private final InstrumentRepository instrumentRepository;
    private final MarketCacheService marketCacheService;
    private final FinnhubService finnhubService;

    // Market indices - Note: Most indices require paid subscription
    // Free tier only supports individual US stocks (NYSE/NASDAQ)
    // Indices like ^GSPC, ^DJI require paid plan
    // For free tier, return empty list or use mock data
    @SuppressWarnings("unused") // Kept for future use with paid plan
    private static final Map<String, MarketIndexInfo> MARKET_INDICES = Map.of(
        // Note: These require paid subscription, but keeping structure for future use
        "SP_500", new MarketIndexInfo("^GSPC", "S&P 500", "NYSE"),
        "DOW_JONES", new MarketIndexInfo("^DJI", "Dow Jones Industrial Average", "NYSE"),
        "NASDAQ", new MarketIndexInfo("^IXIC", "NASDAQ Composite", "NASDAQ")
    );

    private record MarketIndexInfo(String symbol, String name, String exchange) {}

    /**
     * Get all major market indices (with caching)
     * Fetches real-time data from Finnhub API
     */
    public List<MarketIndexResponse> getMarketIndices() {
        // Try cache first
        List<MarketIndexResponse> cached = marketCacheService.getMarketIndices();
        if (cached != null && !cached.isEmpty()) {
            log.info("Returning {} market indices from cache", cached.size());
            return cached;
        }

        // Cache miss - Fetch from Finnhub API
        // Note: Market indices require paid subscription on Finnhub
        // Free tier only supports individual US stocks
        log.info("Cache miss - Market indices require paid subscription. Free tier supports US stocks only.");
        log.warn("To get market indices, upgrade to Finnhub paid plan or use alternative data source.");
        
        // Return empty list for free tier
        // Optionally, you can return mock data for development
        List<MarketIndexResponse> indices = new ArrayList<>();
        
        // Uncomment below to try fetching (will fail on free tier)
        /*
        for (Map.Entry<String, MarketIndexInfo> entry : MARKET_INDICES.entrySet()) {
            MarketIndexInfo indexInfo = entry.getValue();
            String finnhubSymbol = indexInfo.symbol();
            
            try {
                log.debug("Fetching quote for index {} with Finnhub symbol: {}", indexInfo.name(), finnhubSymbol);
                FinnhubQuoteResponse quote = finnhubService.getQuoteSync(finnhubSymbol);
                
                if (quote == null) {
                    log.warn("Finnhub returned null quote for index: {} (symbol: {})", indexInfo.name(), finnhubSymbol);
                    log.warn("Market indices require paid subscription");
                    continue;
                }
                
                if (quote.getCurrentPrice() != null && quote.getCurrentPrice() > 0) {
                    BigDecimal currentPrice = BigDecimal.valueOf(quote.getCurrentPrice());
                    BigDecimal previousClose = quote.getPreviousClose() != null 
                        ? BigDecimal.valueOf(quote.getPreviousClose()) 
                        : currentPrice;
                    BigDecimal change = currentPrice.subtract(previousClose);
                    BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) > 0
                        ? change.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;
                    
                    MarketIndexResponse index = MarketIndexResponse.builder()
                        .symbol(indexInfo.symbol())
                        .name(indexInfo.name())
                        .currentPrice(currentPrice)
                        .change(change)
                        .changePercent(changePercent)
                        .previousClose(previousClose)
                        .volume(0L) // Finnhub quote doesn't provide volume for indices
                        .exchange(indexInfo.exchange())
                        .build();
                    
                    indices.add(index);
                    log.debug("Fetched index data for {}: {}", indexInfo.name(), currentPrice);
                } else {
                    log.warn("No quote data available for index: {} (symbol: {})", indexInfo.name(), finnhubSymbol);
                    log.warn("Possible reasons: 1) Finnhub free tier doesn't support Indian markets, 2) Invalid symbol, 3) Market closed");
                }
            } catch (Exception e) {
                log.error("Error fetching index data for {} (symbol: {}): {}", 
                    indexInfo.name(), finnhubSymbol, e.getMessage(), e);
            }
        }
        */
        
        // For free tier, return empty list
        // In production with paid plan, uncomment the above code
        log.info("Returning empty indices list (free tier limitation)");
        return indices;
    }

    /**
     * Get trending stocks based on volume and price change (with caching)
     */
    public List<TrendingStockResponse> getTrendingStocks(int limit) {
        // Try cache first
        List<TrendingStockResponse> cached = marketCacheService.getTrendingStocks();
        if (cached != null && !cached.isEmpty()) {
            log.info("Returning {} trending stocks from cache", cached.size());
            // Apply limit if cached list is larger
            return cached.size() > limit ? cached.subList(0, limit) : cached;
        }

        // Cache miss - Fetch from Finnhub API
        log.info("Cache miss - Fetching trending stocks from Finnhub (free tier: US markets only)");
        try {
            // Filter to only US markets (free tier supported)
            List<Instrument> activeInstruments = instrumentRepository.findByIsActiveTrue().stream()
                .filter(instrument -> isFreeTierSupported(instrument.getExchange()))
                .toList();
            log.info("Found {} US market instruments for trending stocks (free tier)", activeInstruments.size());
            
            if (activeInstruments.isEmpty()) {
                log.warn("No US market instruments found. Free tier only supports NYSE and NASDAQ.");
                return Collections.emptyList();
            }
            
            // Convert instruments to Finnhub symbols and fetch quotes
            List<String> finnhubSymbols = activeInstruments.stream()
                .map(instrument -> convertToFinnhubSymbol(instrument))
                .filter(symbol -> symbol != null)
                .toList();
            
            if (finnhubSymbols.isEmpty()) {
                log.warn("No valid Finnhub symbols found");
                return Collections.emptyList();
            }
            
            // Fetch batch quotes from Finnhub with longer timeout (rate limiting adds delays)
            Map<String, FinnhubQuoteResponse> quotes = null;
            try {
                quotes = finnhubService.getBatchQuotes(finnhubSymbols)
                    .block(java.time.Duration.ofSeconds(60)); // Increased timeout for rate-limited requests
            } catch (Exception e) {
                log.error("Error fetching batch quotes from Finnhub: {}", e.getMessage());
                // Return empty list instead of crashing
                return Collections.emptyList();
            }
            
            if (quotes == null || quotes.isEmpty()) {
                log.warn("No quotes received from Finnhub (may be rate limited)");
                return Collections.emptyList();
            }
            
            // Map quotes to stock prices
            Map<String, StockPriceResponse> stockPrices = new HashMap<>();
            for (Instrument instrument : activeInstruments) {
                String finnhubSymbol = convertToFinnhubSymbol(instrument);
                if (finnhubSymbol != null && quotes.containsKey(finnhubSymbol)) {
                    FinnhubQuoteResponse quote = quotes.get(finnhubSymbol);
                    if (quote != null && quote.getCurrentPrice() != null) {
                        StockPriceResponse stockPrice = buildStockPriceResponse(instrument, quote);
                        stockPrices.put(instrument.getSymbol(), stockPrice);
                    }
                }
            }
            
            log.info("Fetched {} stock prices from Finnhub", stockPrices.size());
            
            // Filter and sort by trending criteria
            List<TrendingStockResponse> trending = stockPrices.values().stream()
                .filter(price -> price.getChangePercent() != null && price.getVolume() != null)
                .map(price -> {
                    String trendReason = determineTrendReason(price);
                    return TrendingStockResponse.builder()
                        .symbol(price.getSymbol())
                        .name(price.getName())
                        .exchange(price.getExchange())
                        .currentPrice(price.getCurrentPrice())
                        .changePercent(price.getChangePercent())
                        .volume(price.getVolume())
                        .trendReason(trendReason)
                        .build();
                })
                .sorted((a, b) -> {
                    // Sort by: high volume + significant price change
                    int volumeCompare = Long.compare(b.getVolume(), a.getVolume());
                    if (volumeCompare != 0) return volumeCompare;
                    return b.getChangePercent().compareTo(a.getChangePercent());
                })
                .limit(limit)
                .collect(Collectors.toList());
            
            // Cache the results
            if (!trending.isEmpty()) {
                marketCacheService.cacheTrendingStocks(trending);
            }
            
            log.info("Returning {} trending stocks", trending.size());
            return trending;
        } catch (Exception e) {
            log.error("Error in getTrendingStocks", e);
            throw new RuntimeException("Failed to fetch trending stocks: " + e.getMessage(), e);
        }
    }

    private String determineTrendReason(StockPriceResponse price) {
        if (price.getChangePercent() == null) return "UNKNOWN";
        
        BigDecimal changePercent = price.getChangePercent();
        boolean isHighVolume = price.getVolume() != null && price.getVolume() > 1000000; // 1M+ volume
        
        if (changePercent.compareTo(BigDecimal.valueOf(5)) > 0) {
            return isHighVolume ? "PRICE_SURGE_HIGH_VOLUME" : "PRICE_SURGE";
        } else if (changePercent.compareTo(BigDecimal.valueOf(-5)) < 0) {
            return isHighVolume ? "PRICE_DROP_HIGH_VOLUME" : "PRICE_DROP";
        } else if (isHighVolume) {
            return "HIGH_VOLUME";
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Get stocks with real-time prices (paginated, with caching)
     */
    public List<StockPriceResponse> getStocksWithPrices(int page, int size) {
        // Try cache first
        List<StockPriceResponse> cached = marketCacheService.getStocksWithPrices(page, size);
        if (cached != null && !cached.isEmpty()) {
            log.info("Returning {} stocks from cache (page={}, size={})", cached.size(), page, size);
            return cached;
        }

        // Cache miss - Fetch from Finnhub API
        log.info("Cache miss - Fetching stocks with prices from Finnhub (page={}, size={}, free tier: US markets only)", page, size);
        try {
            // Filter to only US markets (free tier supported)
            List<Instrument> allInstruments = instrumentRepository.findByIsActiveTrue().stream()
                .filter(instrument -> isFreeTierSupported(instrument.getExchange()))
                .toList();
            log.info("Found {} US market instruments (free tier)", allInstruments.size());
            
            if (allInstruments.isEmpty()) {
                log.warn("No US market instruments found. Free tier only supports NYSE and NASDAQ.");
                return Collections.emptyList();
            }
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, allInstruments.size());
            
            if (start >= allInstruments.size()) {
                log.info("Page {} is beyond available instruments, returning empty list", page);
                return Collections.emptyList();
            }
            
            List<Instrument> paginatedInstruments = allInstruments.subList(start, end);
            log.info("Fetching prices for {} US market instruments (page {}, size {})", paginatedInstruments.size(), page, size);
            
            // Convert instruments to Finnhub symbols and fetch quotes
            List<String> finnhubSymbols = paginatedInstruments.stream()
                .map(instrument -> convertToFinnhubSymbol(instrument))
                .filter(symbol -> symbol != null)
                .toList();
            
            if (finnhubSymbols.isEmpty()) {
                log.warn("No valid Finnhub symbols found for page {}", page);
                return Collections.emptyList();
            }
            
            // Fetch batch quotes from Finnhub with longer timeout (rate limiting adds delays)
            Map<String, FinnhubQuoteResponse> quotes = null;
            try {
                quotes = finnhubService.getBatchQuotes(finnhubSymbols)
                    .block(java.time.Duration.ofSeconds(60)); // Increased timeout for rate-limited requests
            } catch (Exception e) {
                log.error("Error fetching batch quotes from Finnhub: {}", e.getMessage());
                // Return empty list instead of crashing
                return Collections.emptyList();
            }
            
            if (quotes == null || quotes.isEmpty()) {
                log.warn("No quotes received from Finnhub for page {} (may be rate limited)", page);
                return Collections.emptyList();
            }
            
            // Map quotes to stock prices
            Map<String, StockPriceResponse> prices = new HashMap<>();
            for (Instrument instrument : paginatedInstruments) {
                String finnhubSymbol = convertToFinnhubSymbol(instrument);
                if (finnhubSymbol != null && quotes.containsKey(finnhubSymbol)) {
                    FinnhubQuoteResponse quote = quotes.get(finnhubSymbol);
                    if (quote != null && quote.getCurrentPrice() != null) {
                        StockPriceResponse stockPrice = buildStockPriceResponse(instrument, quote);
                        prices.put(instrument.getSymbol(), stockPrice);
                    }
                }
            }
            
            log.info("Fetched {} stock prices from Finnhub for page {}", prices.size(), page);
            
            List<StockPriceResponse> result = new ArrayList<>(prices.values());
            
            // Cache the results
            if (!result.isEmpty()) {
                marketCacheService.cacheStocksWithPrices(page, size, result);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error in getStocksWithPrices", e);
            throw new RuntimeException("Failed to fetch stocks with prices: " + e.getMessage(), e);
        }
    }

    /**
     * Get market summary for dashboard
     */
    public MarketSummaryResponse getMarketSummary() {
        List<MarketIndexResponse> indices = getMarketIndices();
        List<TrendingStockResponse> trending = getTrendingStocks(10);
        long totalActiveStocks = instrumentRepository.findByIsActiveTrue().size();
        
        String marketStatus = determineMarketStatus();
        
        return MarketSummaryResponse.builder()
            .indices(indices)
            .trendingStocks(trending)
            .totalActiveStocks(totalActiveStocks)
            .marketStatus(marketStatus)
            .build();
    }

    private String determineMarketStatus() {
        // Simple implementation - can be enhanced with actual market hours
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // Market hours: 9:15 AM - 3:30 PM IST, Monday to Friday
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return "CLOSED";
        }
        
        if (hour < 9 || (hour == 9 && cal.get(Calendar.MINUTE) < 15)) {
            return "PRE_MARKET";
        } else if (hour > 15 || (hour == 15 && cal.get(Calendar.MINUTE) > 30)) {
            return "POST_MARKET";
        } else {
            return "OPEN";
        }
    }

    /**
     * Convert Instrument to Finnhub symbol format
     * Free tier only supports US markets (NYSE, NASDAQ)
     * US stocks: Use symbol as-is (e.g., "AAPL", "MSFT")
     * US indices: Use ^ prefix (e.g., "^GSPC" for S&P 500)
     */
    private String convertToFinnhubSymbol(Instrument instrument) {
        if (instrument.getSymbol() == null || instrument.getExchange() == null) {
            return null;
        }
        
        String symbol = instrument.getSymbol();
        String exchange = instrument.getExchange().toUpperCase();
        
        // Free tier only supports US markets
        // NYSE and NASDAQ stocks use symbol as-is
        if ("NYSE".equals(exchange) || "NASDAQ".equals(exchange)) {
            return symbol; // US stocks: AAPL, MSFT, GOOGL, etc.
        }
        
        // For indices, check if it's already in index format (starts with ^)
        if (symbol.startsWith("^")) {
            return symbol; // Already in correct format: ^GSPC, ^DJI, etc.
        }
        
        // If exchange is not US, return null (not supported in free tier)
        log.debug("Exchange {} not supported in free tier for symbol {}", exchange, symbol);
        return null;
    }
    
    /**
     * Check if exchange is supported by Finnhub free tier
     * Free tier supports: NYSE, NASDAQ, and US indices
     */
    private boolean isFreeTierSupported(String exchange) {
        if (exchange == null) {
            return false;
        }
        String exchangeUpper = exchange.toUpperCase();
        return "NYSE".equals(exchangeUpper) || 
               "NASDAQ".equals(exchangeUpper) ||
               "CBOE".equals(exchangeUpper); // For VIX
    }

    /**
     * Build StockPriceResponse from Instrument and FinnhubQuoteResponse
     */
    private StockPriceResponse buildStockPriceResponse(Instrument instrument, FinnhubQuoteResponse quote) {
        BigDecimal currentPrice = BigDecimal.valueOf(quote.getCurrentPrice());
        BigDecimal previousClose = quote.getPreviousClose() != null 
            ? BigDecimal.valueOf(quote.getPreviousClose()) 
            : currentPrice;
        BigDecimal change = currentPrice.subtract(previousClose);
        BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) > 0
            ? change.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        // Try to get company profile for additional data
        String finnhubSymbol = convertToFinnhubSymbol(instrument);
        FinnhubCompanyProfileResponse profile = null;
        if (finnhubSymbol != null) {
            try {
                profile = finnhubService.getCompanyProfileSync(finnhubSymbol);
            } catch (Exception e) {
                log.debug("Could not fetch company profile for {}: {}", finnhubSymbol, e.getMessage());
            }
        }
        
        return StockPriceResponse.builder()
            .id(instrument.getId())
            .symbol(instrument.getSymbol())
            .name(profile != null && profile.getName() != null ? profile.getName() : instrument.getName())
            .exchange(instrument.getExchange())
            .sector(instrument.getSector())
            .currentPrice(currentPrice)
            .previousClose(previousClose)
            .change(change)
            .changePercent(changePercent)
            .dayHigh(quote.getHigh() != null ? BigDecimal.valueOf(quote.getHigh()) : currentPrice)
            .dayLow(quote.getLow() != null ? BigDecimal.valueOf(quote.getLow()) : currentPrice)
            .volume(0L) // Quote API doesn't provide volume, would need candles API
            .marketCap(profile != null && profile.getMarketCapitalization() != null
                ? BigDecimal.valueOf(profile.getMarketCapitalization())
                : null)
            .currency(profile != null && profile.getCurrency() != null ? profile.getCurrency() : "USD")
            .lastUpdated(LocalDateTime.now())
            .isActive(instrument.getIsActive())
            .build();
    }
}

