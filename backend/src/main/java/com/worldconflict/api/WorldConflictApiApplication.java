package com.worldconflict.api;

import com.worldconflict.api.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorldConflictApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WorldConflictApiApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner init(AuthService authService) {
        return args -> authService.createTestUser();
    }
}
