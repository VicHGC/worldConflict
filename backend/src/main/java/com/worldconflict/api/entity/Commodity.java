package com.worldconflict.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "commodities")
@Data
public class Commodity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String symbol;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double price;
    
    private String unit;

    @Column(name = "price_change")
    private Double priceChange;

    private Double changePercent;
    
    private Double high;
    
    private Double low;
    
    private Double open;
    
    private Double previousClose;
    
    private Long volume;
    
    @Column(name = "price_history", length = 1000)
    private String priceHistory;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
