package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "Performance Analysis", description = "Endpoints for player performance analysis and predictions")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "Get player performance metrics", description = "Retrieves comprehensive performance metrics for a player")
    @GetMapping("/{playerName}/metrics")
    public ResponseEntity<Object> getPlayerMetrics(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable String playerName) {
        playerName = playerName.replaceAll("[\n\r]", "_");
        Object metrics = analysisService.getPlayerMetrics(playerName);
        return ResponseEntity.ok(metrics);
    }

    @Operation(summary = "Get performance prediction", description = "Predicts player performance for next match considering opponent, venue and position")
    @GetMapping("/{playerName}/prediction")
    public ResponseEntity<Object> getPerformancePrediction(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable String playerName,
            @Parameter(description = "Opponent team name", example = "Real Madrid") @RequestParam String opponent,
            @Parameter(description = "Whether the player is home", example = "true") @RequestParam boolean isHome,
            @Parameter(description = "Player position", example = "FW") @RequestParam String position) {

        playerName = playerName.replaceAll("[\n\r]", "_");
        Object prediction = analysisService.getPerformancePrediction(playerName, opponent, isHome, position);
        return ResponseEntity.ok(prediction);
    }

    @Operation(summary = "Convert player data to analysis format", description = "Converts scraped player data to analysis-ready format")
    @PostMapping("/{playerName}/convert-data")
    public ResponseEntity<Object> convertPlayerData(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable String playerName) {
        playerName = playerName.replaceAll("[\n\r]", "_");
        Object result = analysisService.convertPlayerData(playerName);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get comparative analysis", description = "Retrieves comparative analysis of player performance across different periods")
    @GetMapping("/{playerName}/comparison")
    public ResponseEntity<Object> getComparativeAnalysis(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable String playerName) {
        playerName = playerName.replaceAll("[\n\r]", "_");
        Object analysis = analysisService.getComparativeAnalysis(playerName);
        return ResponseEntity.ok(analysis);
    }
}