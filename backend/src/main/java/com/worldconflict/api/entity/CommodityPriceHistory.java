package com.worldconflict.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commodity_price_history")
@Data
public class CommodityPriceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;
    
    @Column(name = "price_change", precision = 18, scale = 4)
    private BigDecimal priceChange;
    
    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();
}
