package com.worldconflict.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
public class News {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    private String source;
    
    private String url;
    
    private String imageUrl;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "conflict_zone_id")
    private Long conflictZoneId;
    
    private String category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
