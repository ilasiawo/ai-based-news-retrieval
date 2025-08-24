package com.example.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class GeoUtils {
    
    /**
     * Convert lat/lon to a geo-cluster key for Redis partitioning
     * This creates ~50km x 50km grid clusters
     */
    public String getGeoClusterKey(double lat, double lon) {
        // Round to nearest 0.5 degree (~50km grid)
        int latGrid = (int) Math.round(lat * 2);
        int lonGrid = (int) Math.round(lon * 2);
        return String.format("trending:cluster:%d:%d", latGrid, lonGrid);
    }
    
    /**
     * Get multiple cluster keys for nearby areas (includes neighboring clusters for 50km radius)
     */
    public String[] getNearbyClusterKeys(double lat, double lon) {
        int latGrid = (int) Math.round(lat * 2);
        int lonGrid = (int) Math.round(lon * 2);
        
        // Include more neighboring clusters for 50km coverage
        return new String[] {
            String.format("trending:cluster:%d:%d", latGrid, lonGrid),     // Center
            String.format("trending:cluster:%d:%d", latGrid+1, lonGrid),  // North
            String.format("trending:cluster:%d:%d", latGrid-1, lonGrid),  // South
            String.format("trending:cluster:%d:%d", latGrid, lonGrid+1),  // East
            String.format("trending:cluster:%d:%d", latGrid, lonGrid-1),  // West
            String.format("trending:cluster:%d:%d", latGrid+1, lonGrid+1), // NE
            String.format("trending:cluster:%d:%d", latGrid+1, lonGrid-1), // NW
            String.format("trending:cluster:%d:%d", latGrid-1, lonGrid+1), // SE
            String.format("trending:cluster:%d:%d", latGrid-1, lonGrid-1)  // SW
        };
    }
    
    /**
     * Calculate distance between two points (Haversine formula for better accuracy)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in kilometers
    }
    
    /**
     * Check if a point is within 50km of target location
     */
    public boolean isWithinRadius(double targetLat, double targetLon, 
                                 double pointLat, double pointLon, double radiusKm) {
        return calculateDistance(targetLat, targetLon, pointLat, pointLon) <= radiusKm;
                                 }
    
}
