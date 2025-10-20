package com.dapp.futbol_api.model.dto;

import lombok.Data;

@Data
public class PerformanceMetricsDTO {
    private Long id;
    private String playerName;

    // Offensive metrics
    private Double goalsPerMatch;
    private Double assistsPerMatch;
    private Double goalInvolvement;
    private Double shotsPerMatch;
    private Double shotAccuracy;

    // Distribution metrics
    private Double passAccuracy;
    private Double keyPassesPerMatch;

    // Defensive metrics
    private Double aerialDuelsWon;
    private Double recoveriesPerMatch;

    // Consistency metrics
    private Double averageRating;
    private Double ratingDeviation;
    private Double minutesPerMatch;

    // Advanced prediction metrics
    private Double offensiveImpact;
    private Double performanceTrend;
    private Double goalProbability;
    private Double assistProbability;
}