package com.worldconflict.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String displayName;
    
    @Column(name = "google_id", unique = true)
    private String googleId;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "remember_token")
    private String rememberToken;
    
    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expires")
    private LocalDateTime resetTokenExpires;
}
