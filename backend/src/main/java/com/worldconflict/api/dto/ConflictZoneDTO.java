package com.worldconflict.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConflictZoneDTO {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String description;
    private String region;
    private Boolean isActive;
}
