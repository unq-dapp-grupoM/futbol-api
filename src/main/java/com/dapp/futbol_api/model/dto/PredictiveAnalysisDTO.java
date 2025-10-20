package com.dapp.futbol_api.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PredictiveAnalysisDTO {
    private Long id;
    private String playerName;
    private LocalDate analysisDate;

    // Predictive probabilities
    private Double goalProbability;
    private Double assistProbability;
    private Double highRatingProbability;
    private Double fullMatchProbability;

    // Influence factors
    private Double homeAdvantageFactor;
    private Double opponentFactor;
    private Double positionFactor;
    private Double trendFactor;

    // Recommendations
    private String performancePrediction;
    private Double predictiveScore;
}