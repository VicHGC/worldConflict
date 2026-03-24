package com.worldconflict.api.service;

import com.worldconflict.api.entity.Commodity;
import com.worldconflict.api.repository.CommodityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommodityService {
    
    private final CommodityRepository commodityRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${api.commodity-key:DEMO}")
    private String commodityApiKey;
    
    @Value("${api.oil-key:DEMO}")
    private String oilApiKey;
    
    @Value("${api.alpha-vantage-key:}")
    private String alphaVantageKey;
    
    @Value("${commodity.cache.minutes:30}")
    private int cacheMinutes;
    
    private LocalDateTime lastFetchTime = null;
    private boolean dataLoaded = false;
    
    private static final Map<String, String> COMMODITY_NAMES = new HashMap<>();
    static {
        COMMODITY_NAMES.put("GC=F", "Gold");
        COMMODITY_NAMES.put("SI=F", "Silver");
        COMMODITY_NAMES.put("PL=F", "Platinum");
        COMMODITY_NAMES.put("PD=F", "Palladium");
        COMMODITY_NAMES.put("CL=F", "Crude Oil WTI");
        COMMODITY_NAMES.put("BZ=F", "Brent Crude");
        COMMODITY_NAMES.put("NG=F", "Natural Gas");
        COMMODITY_NAMES.put("HG=F", "Copper");
        COMMODITY_NAMES.put("ZC=F", "Corn");
        COMMODITY_NAMES.put("ZS=F", "Soybeans");
        COMMODITY_NAMES.put("ZW=F", "Wheat");
        COMMODITY_NAMES.put("KE=F", "Kansas Wheat");
        COMMODITY_NAMES.put("KC=F", "Coffee");
        COMMODITY_NAMES.put("SB=F", "Sugar");
        COMMODITY_NAMES.put("CT=F", "Cotton");
        COMMODITY_NAMES.put("LE=F", "Live Cattle");
        COMMODITY_NAMES.put("HE=F", "Lean Hogs");
        COMMODITY_NAMES.put("HO=F", "Heating Oil");
        COMMODITY_NAMES.put("RB=F", "Gasoline");
        COMMODITY_NAMES.put("OJ=F", "Orange Juice");
    }
    
    private static final String[] YAHOO_SYMBOLS = {
        "GC=F", "SI=F", "PL=F", "CL=F", "BZ=F", "NG=F", "HG=F",
        "ZC=F", "ZS=F", "ZW=F", "KC=F", "SB=F", "CT=F"
    };
    
    private static final String[] ALPHA_VANTAGE_SYMBOLS = {
        "XAU", "XAG", "XPT", "XPD", "CL", "BZ", "NG", "HG", 
        "ZC", "ZS", "ZW", "KC", "SB", "CT", "LE", "HE"
    };
    
    @Scheduled(fixedRate = 1800000)
    public void fetchCommodities() {
        if (shouldFetch()) {
            log.info("Fetching commodity prices (cache expired)...");
            boolean success = false;
            
            try {
                success = fetchFromYahooFinance();
            } catch (Exception e) {
                log.error("Yahoo Finance failed: {}", e.getMessage());
            }
            
            if (!success) {
                try {
                    success = fetchFromAlphaVantage();
                } catch (Exception e) {
                    log.error("Alpha Vantage failed: {}", e.getMessage());
                }
            }
            
            if (!success) {
                try {
                    fetchFromOilPriceAPI();
                } catch (Exception e) {
                    log.error("OilPriceAPI failed: {}", e.getMessage());
                }
            }
            
            if (success || commodityRepository.count() > 0) {
                lastFetchTime = LocalDateTime.now();
                dataLoaded = true;
                log.info("Commodity prices updated successfully");
            }
        } else {
            log.info("Using cached commodity data (age: {} minutes)", 
                lastFetchTime != null ? java.time.Duration.between(lastFetchTime, LocalDateTime.now()).toMinutes() : "N/A");
        }
    }
    
    private boolean shouldFetch() {
        if (!dataLoaded) return true;
        if (lastFetchTime == null) return true;
        return java.time.Duration.between(lastFetchTime, LocalDateTime.now()).toMinutes() >= cacheMinutes;
    }
    
    private boolean fetchFromYahooFinance() {
        try {
            String symbols = String.join(",", YAHOO_SYMBOLS);
            String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbols;
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode quotes = root.path("quoteResponse").path("result");
            
            if (quotes.isEmpty()) {
                log.warn("Yahoo Finance returned no data");
                return false;
            }
            
            for (JsonNode quote : quotes) {
                try {
                    String symbol = quote.path("symbol").asText();
                    String name = quote.path("shortName").asText(COMMODITY_NAMES.getOrDefault(symbol, symbol));
                    Double price = quote.path("regularMarketPrice").asDouble();
                    Double previousClose = quote.path("regularMarketPreviousClose").asDouble();
                    Double open = quote.path("regularMarketOpen").asDouble();
                    Double high = quote.path("regularMarketDayHigh").asDouble();
                    Double low = quote.path("regularMarketDayLow").asDouble();
                    
                    if (price == null) continue;
                    
                    Commodity commodity = commodityRepository.findBySymbol(symbol)
                            .orElse(new Commodity());
                    commodity.setSymbol(symbol);
                    commodity.setName(name);
                    commodity.setPrice(price);
                    commodity.setOpen(open);
                    commodity.setHigh(high);
                    commodity.setLow(low);
                    commodity.setPreviousClose(previousClose);
                    commodity.setUnit(getUnitForYahooSymbol(symbol));
                    
                    if (previousClose != null && price != null && previousClose != 0) {
                        double change = price - previousClose;
                        double changePercent = (change / previousClose) * 100;
                        commodity.setPriceChange(change);
                        commodity.setChangePercent(changePercent);
                    }
                    
                    commodity.setUpdatedAt(LocalDateTime.now());
                    commodityRepository.save(commodity);
                    log.info("Updated from Yahoo: {} - ${}", symbol, price);
                } catch (Exception e) {
                    log.warn("Error processing Yahoo quote: {}", e.getMessage());
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Yahoo Finance unavailable: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean fetchFromAlphaVantage() {
        if (alphaVantageKey == null || alphaVantageKey.isEmpty()) {
            log.warn("Alpha Vantage API key not configured");
            return false;
        }
        
        int successCount = 0;
        for (String symbol : ALPHA_VANTAGE_SYMBOLS) {
            try {
                String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaVantageKey;
                String response = restTemplate.getForObject(url, String.class);
                
                JsonNode root = objectMapper.readTree(response);
                JsonNode quote = root.path("Global Quote");
                
                if (!quote.isEmpty() && quote.has("05. price")) {
                    Double price = quote.path("05. price").asDouble();
                    Double high = quote.path("03. high").asDouble();
                    Double low = quote.path("04. low").asDouble();
                    Double open = quote.path("02. open").asDouble();
                    Double prevClose = quote.path("08. previous close").asDouble();
                    
                    if (price == null) continue;
                    
                    Commodity commodity = commodityRepository.findBySymbol(symbol)
                            .orElse(new Commodity());
                    commodity.setSymbol(symbol);
                    commodity.setName(COMMODITY_NAMES.getOrDefault(symbol, symbol));
                    commodity.setPrice(price);
                    commodity.setHigh(high);
                    commodity.setLow(low);
                    commodity.setOpen(open);
                    commodity.setPreviousClose(prevClose);
                    commodity.setUnit(getUnitForSymbol(symbol));
                    
                    if (prevClose != null && price != null && prevClose != 0) {
                        double change = price - prevClose;
                        double changePercent = (change / prevClose) * 100;
                        commodity.setPriceChange(change);
                        commodity.setChangePercent(changePercent);
                    }
                    
                    commodity.setUpdatedAt(LocalDateTime.now());
                    commodityRepository.save(commodity);
                    successCount++;
                    log.info("Updated from Alpha Vantage: {} - ${}", symbol, price);
                }
            } catch (Exception e) {
                log.warn("Error fetching {} from Alpha Vantage: {}", symbol, e.getMessage());
            }
        }
        return successCount > 0;
    }
    
    private void fetchFromOilPriceAPI() {
        try {
            String url = "https://api.oilpriceapi.com/v1/prices/latest/";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Token " + oilApiKey);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.path("data");
            
            for (JsonNode price : data) {
                String code = price.path("code").asText();
                String name = price.path("name").asText();
                Double priceValue = price.path("price").asDouble();
                
                Commodity commodity = commodityRepository.findBySymbol(code)
                        .orElse(new Commodity());
                commodity.setSymbol(code);
                commodity.setName(name);
                commodity.setPriceChange(priceValue);
                commodity.setUnit(price.path("unit").asText());
                commodity.setUpdatedAt(LocalDateTime.now());
                commodityRepository.save(commodity);
            }
        } catch (Exception e) {
            log.warn("OilPriceAPI unavailable: {}", e.getMessage());
        }
    }
    
    private String getUnitForYahooSymbol(String symbol) {
        return switch (symbol) {
            case "GC=F", "SI=F", "PL=F", "PD=F" -> "oz";
            case "CL=F", "BZ=F", "HO=F", "RB=F" -> "bbl";
            case "NG=F" -> "mmBtu";
            case "HG=F" -> "lb";
            case "ZC=F", "ZS=F", "ZW=F", "KE=F" -> "bu";
            case "KC=F", "SB=F", "CT=F", "LE=F", "HE=F", "OJ=F" -> "lb";
            default -> "USD";
        };
    }
    
    private String getUnitForSymbol(String symbol) {
        return switch (symbol) {
            case "XAU", "XAG", "XPT", "XPD" -> "oz";
            case "CL", "BZ", "HO", "RB" -> "bbl";
            case "NG" -> "mmBtu";
            case "HG" -> "lb";
            case "ZC", "ZS", "ZW", "KE" -> "bu";
            case "KC", "SB", "CT", "LE", "HE", "OJ" -> "lb";
            default -> "USD";
        };
    }
    
    public void initializeCommodities() {
        if (commodityRepository.count() == 0) {
            log.info("Initializing commodity data...");
            fetchCommodities();
        }
    }
    
    public List<Commodity> getAllCommodities() {
        return commodityRepository.findAllByOrderBySymbolAsc();
    }
    
    public List<Commodity> getTopCommodities() {
        return commodityRepository.findTop10ByOrderByChangePercentDesc();
    }
}
