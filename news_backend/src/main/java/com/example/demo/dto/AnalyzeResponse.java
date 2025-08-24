package com.example.demo.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyzeResponse {

    private List<Intent> intents;

    @JsonProperty("clarity_score")
    private double clarityScore;

    public AnalyzeResponse() {}

    public List<Intent> getIntents() {
        return intents;
    }

    public void setIntents(List<Intent> intents) {
        this.intents = intents;
    }

    public double getClarityScore() {
        return clarityScore;
    }

    public void setClarityScore(double clarityScore) {
        this.clarityScore = clarityScore;
    }

    public static class Intent {
        private String type; // e.g., category, score, search, source, nearby, trending

        @JsonProperty("intent_weight")
        private double intentWeight;

        private List<String> entities;

        /**
         * For 'nearby' intent type: list of maps containing latitude and longitude as floats/doubles.
         * Example: [{"lat": 12.9716, "long": 77.5946}, {"lat": 12.9352, "long": 77.6245}]
         */
        private List<Map<String, Double>> locations;

        public Intent() {}

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getIntentWeight() {
            return intentWeight;
        }

        public void setIntentWeight(double intentWeight) {
            this.intentWeight = intentWeight;
        }

        public List<String> getEntities() {
            return entities;
        }

        public void setEntities(List<String> entities) {
            this.entities = entities;
        }

        public List<Map<String, Double>> getLocations() {
            return locations;
        }

        public void setLocations(List<Map<String, Double>> locations) {
            this.locations = locations;
        }
    }
}
