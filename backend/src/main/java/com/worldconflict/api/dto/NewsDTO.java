package com.worldconflict.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsDTO {
    private Long id;
    private String title;
    private String description;
    private String source;
    private String url;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private Long conflictZoneId;
    private String category;
}
