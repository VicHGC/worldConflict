package com.worldconflict.api.service;

import com.worldconflict.api.entity.Commodity;
import com.worldconflict.api.entity.CommodityPriceHistory;
import com.worldconflict.api.repository.CommodityPriceHistoryRepository;
import com.worldconflict.api.repository.CommodityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommodityPriceHistoryService {
    
    private final CommodityPriceHistoryRepository priceHistoryRepository;
    private final CommodityRepository commodityRepository;
    
    @Scheduled(fixedRate = 3600000)
    public void savePriceSnapshots() {
        log.info("Saving hourly price snapshots...");
        try {
            List<Commodity> commodities = commodityRepository.findAll();
            
            for (Commodity commodity : commodities) {
                CommodityPriceHistory history = new CommodityPriceHistory();
                history.setSymbol(commodity.getSymbol());
                history.setPrice(commodity.getPrice() != null ? java.math.BigDecimal.valueOf(commodity.getPrice()) : java.math.BigDecimal.ZERO);
                history.setPriceChange(commodity.getPriceChange() != null ? java.math.BigDecimal.valueOf(commodity.getPriceChange()) : java.math.BigDecimal.ZERO);
                history.setChangePercent(commodity.getChangePercent() != null ? java.math.BigDecimal.valueOf(commodity.getChangePercent()) : java.math.BigDecimal.ZERO);
                history.setRecordedAt(LocalDateTime.now());
                
                priceHistoryRepository.save(history);
            }
            
            log.info("Saved {} price snapshots", commodities.size());
        } catch (Exception e) {
            log.error("Error saving price snapshots: {}", e.getMessage());
        }
    }
    
    public List<CommodityPriceHistory> getHistory(String symbol, int days) {
        LocalDateTime after = LocalDateTime.now().minusDays(days);
        return priceHistoryRepository.findHistoryAfter(symbol, after);
    }
    
    public Map<String, List<CommodityPriceHistory>> getAllHistory(int days) {
        LocalDateTime after = LocalDateTime.now().minusDays(days);
        List<CommodityPriceHistory> allHistory = priceHistoryRepository.findByRecordedAtAfterOrderByRecordedAtAsc(after);
        
        return allHistory.stream()
                .collect(Collectors.groupingBy(CommodityPriceHistory::getSymbol));
    }
    
    public void cleanupOldData(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        priceHistoryRepository.deleteByRecordedAtBefore(cutoff);
        log.info("Cleaned up price history older than {} days", daysToKeep);
    }
}
