package com.worldconflict.api.controller;

import com.worldconflict.api.entity.News;
import com.worldconflict.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }
    
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<News>> getNewsByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(newsService.getNewsByZone(zoneId));
    }
}
