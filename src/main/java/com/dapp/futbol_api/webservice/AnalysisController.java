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

    private static final String NEW_LINE_REGEX = "[\n\r]";
    private final AnalysisService analysisService;

    @Operation(summary = "Get player performance metrics", description = "Retrieves comprehensive performance metrics for a player")
    @GetMapping("/{playerName}/performanceMetrics")
    public ResponseEntity<Object> getPlayerPerformanceMetrics(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName) {
        final String sanitizedPlayerName = sanitize(playerName);
        Object performanceMetrics = analysisService.getPlayerPerformanceMetrics(sanitizedPlayerName);
        return ResponseEntity.ok(performanceMetrics);
    }

    @Operation(summary = "Get performance prediction", description = "Predicts player performance for next match considering opponent, venue and position")
    @GetMapping("/{playerName}/prediction")
    public ResponseEntity<Object> getPerformancePrediction(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName,
            @Parameter(description = "Opponent team name", example = "Real Madrid") @RequestParam("opponent") String opponent,
            @Parameter(description = "Whether the player is home", example = "true") @RequestParam("isHome") boolean isHome,
            @Parameter(description = "Player position", example = "FW") @RequestParam("position") String position) {

        final String sanitizedPlayerName = sanitize(playerName);
        final String sanitizedOpponent = sanitize(opponent);
        final String sanitizedPosition = sanitize(position);

        Object prediction = analysisService.getPerformancePrediction(sanitizedPlayerName, sanitizedOpponent, isHome, sanitizedPosition);
        return ResponseEntity.ok(prediction);
    }

    @Operation(summary = "Convert player data to analysis format", description = "Converts scraped player data to analysis-ready format")
    @PostMapping("/{playerName}/convert-data")
    public ResponseEntity<Object> convertPlayerData(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName) {
        final String sanitizedPlayerName = sanitize(playerName);
        Object result = analysisService.convertPlayerData(sanitizedPlayerName);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get comparative analysis", description = "Retrieves comparative analysis of player performance across different periods")
    @GetMapping("/{playerName}/comparison")
    public ResponseEntity<Object> getComparativeAnalysis(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName) {
        final String sanitizedPlayerName = sanitize(playerName);
        Object analysis = analysisService.getComparativeAnalysis(sanitizedPlayerName);
        return ResponseEntity.ok(analysis);
    }

    private String sanitize(String input) {
        return input.replaceAll(NEW_LINE_REGEX, "_");
    }
}