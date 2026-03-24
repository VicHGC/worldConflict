package com.worldconflict.api.repository;

import com.worldconflict.api.entity.CommodityPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommodityPriceHistoryRepository extends JpaRepository<CommodityPriceHistory, Long> {
    
    List<CommodityPriceHistory> findBySymbolOrderByRecordedAtDesc(String symbol);
    
    List<CommodityPriceHistory> findBySymbolAndRecordedAtAfterOrderByRecordedAtAsc(
            String symbol, LocalDateTime after);
    
    @Query("SELECT h FROM CommodityPriceHistory h WHERE h.symbol = :symbol AND h.recordedAt >= :after ORDER BY h.recordedAt ASC")
    List<CommodityPriceHistory> findHistoryAfter(@Param("symbol") String symbol, @Param("after") LocalDateTime after);
    
    @Query("SELECT h FROM CommodityPriceHistory h WHERE h.recordedAt >= :after ORDER BY h.recordedAt ASC")
    List<CommodityPriceHistory> findByRecordedAtAfterOrderByRecordedAtAsc(@Param("after") LocalDateTime after);
    
    void deleteByRecordedAtBefore(LocalDateTime before);
}
