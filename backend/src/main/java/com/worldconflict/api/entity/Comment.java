package com.worldconflict.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(name = "news_id", nullable = false)
    private Long newsId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
