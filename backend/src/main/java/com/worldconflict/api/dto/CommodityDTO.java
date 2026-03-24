package com.worldconflict.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommodityDTO {
    private Long id;
    private String symbol;
    private String name;
    private Double price;
    private String unit;
    private Double change;
    private Double changePercent;
    private LocalDateTime updatedAt;
}
