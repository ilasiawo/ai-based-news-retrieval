package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.NewsArticle;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID> {

    // Use GIN index on category (text[] column)
 @Query(value =
  "SELECT * FROM news_articles " +
  "WHERE EXISTS ( " +
  "  SELECT 1 FROM unnest(category) AS c " +
  "  WHERE LOWER(c) LIKE LOWER(:pattern) " +
  ") " +
  "ORDER BY publication_date DESC",
  nativeQuery = true)
List<NewsArticle> findByCategory(@Param("pattern") String pattern);



    // Use btree index for relevance_score DESC
    @Query(value = "SELECT * FROM news_articles WHERE relevance_score >= :threshold ORDER BY relevance_score DESC", nativeQuery = true)
    List<NewsArticle> findByRelevanceScore(@Param("threshold") double threshold);

    // Use GIN index on tsvector for full-text search on title + description
    @Query(value = "SELECT *, " +
               "GREATEST( " +
               "  ts_rank(to_tsvector('english', LOWER(title || ' ' || coalesce(description, ''))), plainto_tsquery('english', LOWER(:query))), " +
               "  similarity(LOWER(title), LOWER(:query)), " +
               "  similarity(LOWER(coalesce(description, '')), LOWER(:query)) " +
               ") AS rank_score " +
               "FROM news_articles " +
               "WHERE to_tsvector('english', LOWER(title || ' ' || coalesce(description, ''))) @@ plainto_tsquery('english', LOWER(:query)) " +
               "   OR similarity(LOWER(title), LOWER(:query)) > 0.3 " +
               "   OR similarity(LOWER(coalesce(description, '')), LOWER(:query)) > 0.3 " +
               "ORDER BY rank_score DESC, publication_date DESC",
       nativeQuery = true)
List<NewsArticle> findBySearch(@Param("query") String query);



    // Use btree index on source_name
@Query(value = "SELECT * FROM news_articles " +
               "WHERE LOWER(source_name) LIKE CONCAT('%', LOWER(:source), '%') " +
               "ORDER BY publication_date DESC",
       nativeQuery = true)
List<NewsArticle> findBySource(@Param("source") String source);


    // Use GiST index on geo_point for nearby search with spatial function
@Query(value = "SELECT *, " +
               "ST_Distance(geo_point, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) AS distance " +
               "FROM news_articles " +
               "WHERE ST_DWithin(geo_point, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radius_km * 1000) " +
               "ORDER BY distance ASC",
       nativeQuery = true)
List<NewsArticle> findNearby(@Param("lon") double longitude,
                             @Param("lat") double latitude,
                             @Param("radius_km") double radiusKm);


}
