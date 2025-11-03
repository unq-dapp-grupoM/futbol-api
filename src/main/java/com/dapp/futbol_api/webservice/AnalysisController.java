package com.dapp.futbol_api.webservice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dapp.futbol_api.service.AnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName,
            Authentication authentication) {
        final String sanitizedPlayerName = sanitize(playerName);
        Object performanceMetrics = analysisService.getPlayerPerformanceMetrics(sanitizedPlayerName, authentication);
        return ResponseEntity.ok(performanceMetrics);
    }

    @Operation(summary = "Get performance prediction", description = "Predicts player performance for next match considering opponent, venue and position")
    @GetMapping("/{playerName}/prediction")
    public ResponseEntity<Object> getPerformancePrediction(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName,
            @Parameter(description = "Opponent team name", example = "Real Madrid") @RequestParam("opponent") String opponent,
            @Parameter(description = "Whether the player is home", example = "true") @RequestParam("isHome") boolean isHome,
            @Parameter(description = "Player position", example = "FW") @RequestParam("position") String position,
            Authentication authentication) {

        final String sanitizedPlayerName = sanitize(playerName);
        final String sanitizedOpponent = sanitize(opponent);
        final String sanitizedPosition = sanitize(position);

        Object prediction = analysisService.getPerformancePrediction(sanitizedPlayerName, sanitizedOpponent, isHome,
                sanitizedPosition, authentication);
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
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName,
            Authentication authentication) {
        final String sanitizedPlayerName = sanitize(playerName);
        Object analysis = analysisService.getComparativeAnalysis(sanitizedPlayerName, authentication);
        return ResponseEntity.ok(analysis);
    }

    @Operation(summary = "Get user query history", description = "Retrieves the query history for a player on a specific date for the authenticated user.")
    @GetMapping("/{playerName}/history")
    public ResponseEntity<Object> getPlayerHistory(
            @Parameter(description = "Name of the player", example = "Lionel Messi") @PathVariable("playerName") String playerName,
            @Parameter(description = "Date of the query (format dd-MM-yyyy)", example = "02-11-2025") @RequestParam("date") String date,
            Authentication authentication) {

        final String sanitizedPlayerName = sanitize(playerName);
        final String sanitizedDate = sanitize(date);
        Object history = analysisService.getPlayerHistory(sanitizedPlayerName, sanitizedDate, authentication);
        return ResponseEntity.ok(history);
    }

    private String sanitize(String input) {
        return input.replaceAll(NEW_LINE_REGEX, "_");
    }
}