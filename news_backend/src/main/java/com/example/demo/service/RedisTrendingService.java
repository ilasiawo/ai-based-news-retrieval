package com.example.demo.service;

import com.example.demo.dto.NewsArticleDto;
import com.example.demo.dto.EventType;
import com.example.demo.entity.NewsArticle;
import com.example.demo.repository.NewsArticleRepository;
import com.example.demo.utils.GeoUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisTrendingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final NewsArticleRepository newsArticleRepository;
    private final NewsService newsService;
    private final GeoUtils geoUtils;

    // Decay factor for recency bias: tweak lambda to favor recent events
    private static final double LAMBDA = 0.001;

    public RedisTrendingService(RedisTemplate<String, Object> redisTemplate,
                               NewsArticleRepository newsArticleRepository,
                               NewsService newsService,
                               GeoUtils geoUtils) {
        this.redisTemplate = redisTemplate;
        this.newsArticleRepository = newsArticleRepository;
        this.newsService = newsService;
        this.geoUtils = geoUtils;
    }

    /**
     * Update trending score for an article when a user event occurs.
     * Stores the raw score and last event timestamp in Redis.
     */
    public void updateTrendingScore(UUID articleId, EventType eventType, double userLat, double userLon) {
        String clusterKey = geoUtils.getGeoClusterKey(userLat, userLon);
        double scoreIncrement = getEventWeight(eventType);

        long now = Instant.now().getEpochSecond();
        String redisValue = scoreIncrement + ":" + now; // store as "rawScore:lastEventTime"

        redisTemplate.opsForZSet().incrementScore(clusterKey, articleId.toString(), scoreIncrement);
        redisTemplate.opsForValue().set(articleId.toString() + ":time", now); // optional separate timestamp if needed
        redisTemplate.expire(clusterKey, java.time.Duration.ofHours(24));

    }

    /**
     * Get trending articles near a location.
     * Applies recency bias using exponential decay based on last event timestamp.
     * Fetches extra range (4x limit) to avoid discarding recent low-score events.
     * TODO: Can use a scheduled job to periodically maintain timed scores in Redis for performance.
     */
    public List<NewsArticleDto> getTrendingArticles(double lat, double lon, int limit) throws Exception {
        String[] clusterKeys = geoUtils.getNearbyClusterKeys(lat, lon);

        Map<String, Double> combinedTimedScores = new HashMap<>();
        long now = Instant.now().getEpochSecond();

        for (String clusterKey : clusterKeys) {
            Set<ZSetOperations.TypedTuple<Object>> trendingData =
                    redisTemplate.opsForZSet().reverseRangeWithScores(clusterKey, 0, limit * 4); // fetch extra range


            if (trendingData != null) {
    for (ZSetOperations.TypedTuple<Object> tuple : trendingData) {
        String articleId = tuple.getValue().toString();
        Double rawScore = tuple.getScore();

        // Fetch last event timestamp
        Object lastEventTimeObj = redisTemplate.opsForValue().get(articleId + ":time");
        long lastEventTime;

        if (lastEventTimeObj == null) {
            lastEventTime = now;
        } else if (lastEventTimeObj instanceof Number) {
            lastEventTime = ((Number) lastEventTimeObj).longValue();
        } else {
            lastEventTime = now; 
        }

        double deltaTime = now - lastEventTime;
        double timedScore = rawScore * Math.exp(-LAMBDA * deltaTime);
        
        System.out.println(articleId+ " " + timedScore);

            combinedTimedScores.merge(articleId, timedScore, Double::sum);

        // Optionally update timed score back to Redis
        redisTemplate.opsForZSet().add(clusterKey, articleId, timedScore);

    }
}

        }
  

        if (combinedTimedScores.isEmpty()) return Collections.emptyList();

        List<String> topArticleIds = combinedTimedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

                System.out.println(topArticleIds); // Debugging line

        List<UUID> articleUuids = topArticleIds.stream().map(UUID::fromString).collect(Collectors.toList());
        List<NewsArticle> articles = newsArticleRepository.findAllById(articleUuids);

        Map<UUID, NewsArticle> articlesMap = articles.stream()
        .collect(Collectors.toMap(NewsArticle::getId, a -> a));

List<NewsArticle> sortedArticles = articleUuids.stream()
        .map(articlesMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        return newsService.enrichArticlesWithSummaries(sortedArticles);
    }

    private double getEventWeight(EventType eventType) {
        if (eventType == null) return 1.0;
        return eventType.getScore();
    }
}
