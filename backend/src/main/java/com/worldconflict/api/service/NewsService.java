package com.worldconflict.api.service;

import com.worldconflict.api.entity.News;
import com.worldconflict.api.repository.NewsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    
    private final NewsRepository newsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${api.news-key:DEMO}")
    private String newsApiKey;
    
    private static final String[] CONFLICT_KEYWORDS = {"Ukraine", "Russia", "Iran", "war", "conflict", "military", "invasion"};
    
    @Scheduled(fixedRate = 900000)
    public void fetchNews() {
        log.info("Fetching news about conflicts...");
        try {
            fetchNewsFromApi("Ukraine war");
            fetchNewsFromApi("Iran conflict");
            fetchNewsFromApi("Russia military");
        } catch (Exception e) {
            log.error("Error fetching news: {}", e.getMessage());
            saveSampleNews();
        }
    }
    
    private void fetchNewsFromApi(String query) {
        try {
            String url = "https://newsdata.io/api/1/news?apikey=" + newsApiKey + "&q=" + query + "&language=en&category=politics";
            String response = restTemplate.getForObject(url, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");
            
            if (results.isArray()) {
                List<News> newsList = new ArrayList<>();
                for (JsonNode article : results) {
                    News news = new News();
                    news.setTitle(article.path("title").asText());
                    news.setDescription(article.path("description").asText());
                    news.setSource(article.path("source_id").asText());
                    news.setUrl(article.path("url").asText());
                    news.setImageUrl(article.path("image_url").asText());
                    news.setCategory(article.path("category").asText());
                    news.setPublishedAt(LocalDateTime.now());
                    news.setConflictZoneId(determineConflictZone(news.getTitle() + " " + news.getDescription()));
                    newsList.add(news);
                }
                if (!newsList.isEmpty()) {
                    newsRepository.saveAll(newsList);
                    log.info("Saved {} news articles", newsList.size());
                } else {
                    // Fallback: seed sample news if API returns no results
                    saveSampleNews();
                    log.info("No API news found, seeded sample news");
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch from API, using sample data: {}", e.getMessage());
            saveSampleNews();
        }
    }
    
    private Long determineConflictZone(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("ukraine") || lowerText.contains("russia")) {
            return 1L;
        } else if (lowerText.contains("iran") || lowerText.contains("israel") || lowerText.contains("middle east")) {
            return 2L;
        }
        return null;
    }
    
    private void saveSampleNews() {
        List<News> sampleNews = List.of(
            createNews("Russia-Ukraine War Updates", "Latest developments in the ongoing conflict between Russia and Ukraine", "Reuters", 1L),
            createNews("Ukraine Military Aid", "Western allies announce new military aid packages for Ukraine", "BBC News", 1L),
            createNews("Iran Nuclear Program Updates", "International tensions rise over Iran's nuclear program", "NY Times", 2L),
            createNews("Middle East Conflict", "Escalating tensions in the Middle East region", "Al Jazeera", 2L),
            createNews("Ukraine Reconstruction Efforts", "International community discusses Ukraine reconstruction", "The Guardian", 1L),
            createNews("Iran Sanctions Discussion", "World powers discuss new sanctions on Iran", "Reuters", 2L),
            createNews("Russia Military Exercises", "Russia conducts new military exercises near border regions", "AP News", 1L),
            createNews("Iran Regional Influence", "Analysis of Iran's regional influence and activities", "Financial Times", 2L)
        );
        newsRepository.saveAll(sampleNews);
    }
    
    private News createNews(String title, String description, String source, Long zoneId) {
        News news = new News();
        news.setTitle(title);
        news.setDescription(description);
        news.setSource(source);
        news.setPublishedAt(LocalDateTime.now());
        news.setConflictZoneId(zoneId);
        news.setCategory("politics");
        return news;
    }
    
    public List<News> getAllNews() {
        return newsRepository.findTop30ByOrderByPublishedAtDesc();
    }
    
    public List<News> getNewsByZone(Long zoneId) {
        return newsRepository.findByConflictZoneIdOrderByPublishedAtDesc(zoneId);
    }
}
