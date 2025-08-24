package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.dto.AnalyzeResponse;
import com.example.demo.dto.NewsArticleDto;
import com.example.demo.entity.LLMMapping;
import com.example.demo.entity.NewsArticle;
import com.example.demo.repository.LLMMappingRepository;
import com.example.demo.repository.NewsArticleRepository;

@Service
public class NewsService {

    private final NewsArticleRepository newsArticleRepository;
    private final LLMMappingRepository llmMappingRepository;
    private final LLMService llmService;

    public NewsService(NewsArticleRepository newsArticleRepository,
                       LLMMappingRepository llmMappingRepository,
                       LLMService llmService) {
        this.newsArticleRepository = newsArticleRepository;
        this.llmMappingRepository = llmMappingRepository;
        this.llmService = llmService;
    }

    /**
     * Processes user news query by calling LLM to extract intents and clarity score,
     * fetches articles for each intent combining intent scores,
     * ranks articles by combined score, limits and enriches with summaries.
     */
    public List<NewsArticleDto> processUserQuery(String userQuery) throws Exception {
        AnalyzeResponse analyzeResponse = llmService.analyzeQuery(userQuery);

        Map<UUID, ArticleWithScore> articleScoreMap = new LinkedHashMap<>();
        double clarityScore = analyzeResponse.getClarityScore();

        for (AnalyzeResponse.Intent intent : analyzeResponse.getIntents()) {
            List<NewsArticle> articlesForIntent = null;
            try {
             articlesForIntent = fetchArticlesByIntent(intent, clarityScore);
            } catch (Exception e) {
                System.err.println("Error fetching articles for intent " + intent.getType() + ": " + e.getMessage());
                continue;
            }
            double intentWeight = intent.getIntentWeight();

            for (NewsArticle article : articlesForIntent) {
                articleScoreMap.compute(article.getId(), (id, existing) -> {
                    if (existing == null) {
                        return new ArticleWithScore(article, intentWeight, clarityScore);
                    }
                    existing.addIntentScore(intentWeight);
                    return existing;
                });
            }
        }

        List<NewsArticle> topArticles = getTopArticlesWithIntentScore(articleScoreMap);
        return enrichArticlesWithSummaries(topArticles);
    }

    /**
     * Fetch and enrich articles for APIs other than query:
     * fetch by intent type and parameters,
     * rank by relevance score, limit top 5, enrich summaries.
     */
public List<NewsArticleDto> fetchAndEnrichArticles(String intentType, List<String> entities,
                                                      double lat, double lon, double threshold) throws Exception {
        List<NewsArticle> articles = fetchByIntentType(intentType, entities, lat, lon, threshold);
        List<NewsArticle> topArticles = getTopArticlesByRelevance(articles, 5);
        return enrichArticlesWithSummaries(topArticles);
    }

    /**
     * Fetch articles matching a single intent.
     * For "nearby" intent, handle multiple locations aggregating results.
     */
    private List<NewsArticle> fetchArticlesByIntent(AnalyzeResponse.Intent intent, double clarityScore) {
        if ("nearby".equalsIgnoreCase(intent.getType()) 
                && intent.getLocations() != null 
                && !intent.getLocations().isEmpty()) {
            List<NewsArticle> combinedNearbyArticles = new ArrayList<>();
            for (Map<String, Double> location : intent.getLocations()) {
                Double lat = location.get("lat");
                Double lon = location.get("long");
                if (lat != null && lon != null) {
                    combinedNearbyArticles.addAll(newsArticleRepository.findNearby(lon, lat, 50)); // radius 10km default
                }
            }
            return combinedNearbyArticles;
        }
        return fetchByIntentType(intent.getType(), intent.getEntities(), 0, 0, clarityScore);
    }

    /**
     * Dispatch fetching by intent type to specific repository methods.
     */
    private List<NewsArticle> fetchByIntentType(String intentType, List<String> entities,
                                                double lat, double lon, double threshold) {

                                                    try {
        switch (intentType.toLowerCase()) {
            case "category":
                return fetchMultipleCategories(entities);
            case "score":
                return newsArticleRepository.findByRelevanceScore(threshold);
            case "search":
                return fetchMultipleSearchKeywords(entities);
            case "source":
                return fetchMultipleSources(entities);
            case "nearby":
                return newsArticleRepository.findNearby(lon, lat, threshold);
            case "trending":
                return fetchTrending();
            default:
                return Collections.emptyList();
        }
    } catch (Exception e) {
        System.err.println("Error fetching articles for intent type " + intentType + ": " + e.getMessage());
        return Collections.emptyList();
    }
}  

    private List<NewsArticle> fetchMultipleCategories(List<String> categories) {
        List<NewsArticle> result = new ArrayList<>();
        if (categories != null) {
            for (String category : categories) {
                result.addAll(newsArticleRepository.findByCategory(category));
            }
        }
        return result;
    }

    private List<NewsArticle> fetchMultipleSearchKeywords(List<String> keywords) {
        List<NewsArticle> result = new ArrayList<>();
        if (keywords != null) {
            for (String kw : keywords) {
                result.addAll(newsArticleRepository.findBySearch(kw));
            }
        }
        return result;
    }

    private List<NewsArticle> fetchMultipleSources(List<String> sources) {
        List<NewsArticle> result = new ArrayList<>();
        if (sources != null) {
            for (String src : sources) {
                result.addAll(newsArticleRepository.findBySource(src));
            }
        }
        return result;
    }

    private List<NewsArticle> fetchTrending() {
        // Trending logic placeholder
        return Collections.emptyList();
    }

    /**
     * Return top articles ranked by combined intent-weighted & clarity-weighted score.
     */
    private List<NewsArticle> getTopArticlesWithIntentScore(Map<UUID, ArticleWithScore> articleScoreMap) {
        return articleScoreMap.values().stream()
                .peek(ArticleWithScore::calculateFinalScore)
                .sorted(Comparator.comparingDouble(ArticleWithScore::getFinalScore).reversed())
                .limit(5)
                .map(ArticleWithScore::getArticle)
                .toList();
    }

    /**
     * Return top articles ranked purely by relevance score.
     */
    private List<NewsArticle> getTopArticlesByRelevance(List<NewsArticle> articles, int limit) {
        return articles.stream()
                .sorted(Comparator.comparingDouble(NewsArticle::getRelevanceScore).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Enrich articles with cached summaries or generate/save if missing.
     */
    public List<NewsArticleDto> enrichArticlesWithSummaries(List<NewsArticle> articles) throws Exception {
        List<NewsArticleDto> dtos = new ArrayList<>();

        for (NewsArticle article : articles) {
            NewsArticleDto dto = mapToDto(article);

            String summary = llmMappingRepository.findById(article.getId())
                    .map(LLMMapping::getLlmSummary)
                    .filter(s -> !s.isEmpty())
                    .orElseGet(() -> {
                        try {
                            String generated = llmService.generateSummary(article.getTitle(), article.getDescription(), article.getUrl());
                            saveSummary(article, generated);
                            return generated;
                        } catch (Exception e) {
                            System.err.println("Error generating/saving summary for article ID " + article.getId() + ": " + e.getMessage());
                            return "";
                        }
                    });

            dto.setLlmSummary(summary);
            dtos.add(dto);
        }
        return dtos;
    }

    private void saveSummary(NewsArticle article, String summary) throws Exception {
        NewsArticle managedArticle = newsArticleRepository.findById(article.getId())
                .orElseThrow(() -> new IllegalStateException("Article not found for ID " + article.getId()));

        LLMMapping mapping = llmMappingRepository.findById(article.getId())
                .orElse(new LLMMapping());

        mapping.setNewsArticle(managedArticle);
        mapping.setLlmSummary(summary);
        llmMappingRepository.save(mapping);
    }

    private NewsArticleDto mapToDto(NewsArticle article) {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setId(article.getId().toString());
        dto.setTitle(article.getTitle());
        dto.setDescription(article.getDescription());
        dto.setUrl(article.getUrl());
        dto.setPublicationDate(article.getPublicationDate());  
        dto.setSourceName(article.getSourceName());
        dto.setCategory(article.getCategory());
        dto.setRelevanceScore(article.getRelevanceScore());
        return dto;
    }

    /**
     * Helper class to accumulate intent scores and calculate final weighted score per article.
     */
    private static class ArticleWithScore {
        private final NewsArticle article;
        private double accumulatedIntentScore = 0.0;
        private final double clarityScore;
        private double finalScore = 0.0;

    public ArticleWithScore(NewsArticle article, double initialIntentScore, double clarityScore) {
            this.article = article;
            this.accumulatedIntentScore = initialIntentScore;
            this.clarityScore = clarityScore;
        }

        public void addIntentScore(double additionalScore) {
            this.accumulatedIntentScore += additionalScore;
        }

    public void calculateFinalScore() {
            double relevanceScore = article.getRelevanceScore();
            this.finalScore = (accumulatedIntentScore * clarityScore) + (relevanceScore * (1 - clarityScore));
            this.article.setRelevanceScore(this.finalScore);
        }

        public double getFinalScore() {
            return finalScore;
        }

        public NewsArticle getArticle() {
            return article;
        }
    }
}
