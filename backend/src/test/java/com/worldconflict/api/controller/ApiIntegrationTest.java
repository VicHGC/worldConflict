package com.worldconflict.api.controller;

import com.worldconflict.api.config.JwtService;
import com.worldconflict.api.entity.Commodity;
import com.worldconflict.api.entity.News;
import com.worldconflict.api.repository.CommodityRepository;
import com.worldconflict.api.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private NewsRepository newsRepository;
    
    @Autowired
    private CommodityRepository commodityRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @BeforeEach
    void setUp() {
        commodityRepository.deleteAll();
        newsRepository.deleteAll();
    }
    
    @Test
    void testGetNews_WithoutAuth() throws Exception {
        News news = new News();
        news.setTitle("Test News");
        news.setDescription("Test Description");
        news.setSource("Test Source");
        newsRepository.save(news);
        
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testGetCommodities_WithoutAuth() throws Exception {
        Commodity commodity = new Commodity();
        commodity.setSymbol("XAU");
        commodity.setName("Gold");
        commodity.setPrice(4500.0);
        commodityRepository.save(commodity);
        
        mockMvc.perform(get("/api/commodities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testGetZones_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testAuthLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }
}