package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.NewsArticleDto;
import com.example.demo.service.NewsService;
import com.example.demo.service.RedisTrendingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;
    private final RedisTrendingService redisTrendingService;

    public NewsController(NewsService newsService, RedisTrendingService redisTrendingService) {
        this.newsService = newsService;
        this.redisTrendingService = redisTrendingService;
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getByCategory(@RequestParam String category) {
        try {
            List<NewsArticleDto> articles =
                    newsService.fetchAndEnrichArticles("category", Collections.singletonList(category), 0, 0, 0);
            return ResponseEntity.ok(new ApiResponse<>(true, "Articles fetched successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch by category", null));
        }
    }

    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getByScore(@RequestParam(defaultValue = "0.7") double threshold) {
        try {
             if (threshold < 0.0 || threshold > 1.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Threshold must be between 0.0 and 1.0", null));
        }
    List<NewsArticleDto> articles =
                    newsService.fetchAndEnrichArticles("score", Collections.emptyList(), 0, 0, threshold);
            return ResponseEntity.ok(new ApiResponse<>(true, "Articles fetched successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch by score", null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> search(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, "Query parameter is required", null));
}
            List<NewsArticleDto> articles =
                    newsService.fetchAndEnrichArticles("search", Collections.singletonList(query), 0, 0, 0);
            return ResponseEntity.ok(new ApiResponse<>(true, "Articles fetched successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to search articles", null));
        }
    }

    @GetMapping("/source")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getBySource(@RequestParam String source) {
        try {
            if (source == null || source.trim().isEmpty()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, "Source parameter is required", null));
}
            List<NewsArticleDto> articles =
                    newsService.fetchAndEnrichArticles("source", Collections.singletonList(source), 0, 0, 0);
            return ResponseEntity.ok(new ApiResponse<>(true, "Articles fetched successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch by source", null));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getNearby(@RequestParam double lat,
                                                                       @RequestParam double lon) {
        try {
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, "Invalid latitude or longitude", null));
}

            List<NewsArticleDto> articles =
                    newsService.fetchAndEnrichArticles("nearby", Collections.emptyList(), lat, lon, 50);
            return ResponseEntity.ok(new ApiResponse<>(true, "Nearby articles fetched successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch nearby articles", null));
        }
    }

    @GetMapping("/query")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> query(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, "Query parameter is required", null));
}
            List<NewsArticleDto> articles = newsService.processUserQuery(query);
            return ResponseEntity.ok(new ApiResponse<>(true, "Query processed successfully", articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to process query", null));
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getTrending(@RequestParam double lat,
                                                                         @RequestParam double lon,
                                                                         @RequestParam(defaultValue = "5") int limit) {
        try {
            List<NewsArticleDto> trendingArticles = redisTrendingService.getTrendingArticles(lat, lon, limit);
            return ResponseEntity.ok(new ApiResponse<>(true, "Trending articles fetched successfully", trendingArticles));
        } catch (Exception e) {
            System.out.println("Error fetching trending articles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch trending articles", null));
        }
    }
}
