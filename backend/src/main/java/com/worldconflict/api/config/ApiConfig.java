package com.worldconflict.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {
    private String newsKey;
    private String commodityKey;
}
