package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.model.QueryHistory;
import com.dapp.futbol_api.service.QueryHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@Tag(name = "Query History", description = "Endpoints for consulting the history of analysis queries")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class QueryHistoryController {

    private final QueryHistoryService queryHistoryService;

    @Operation(summary = "Get query history", description = "Allows to consult the requests for performance, predictions and comparisons made for a given date and player.")
    @GetMapping
    public ResponseEntity<List<QueryHistory>> getQueryHistory(
            @Parameter(description = "Date of the query (format dd/MM/yyyy)", example = "25/05/2024") @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate date,
            @Parameter(description = "Name of the player", example = "Lionel Messi") @RequestParam String playerName) {

        List<QueryHistory> history = queryHistoryService.getHistoryByDateAndPlayer(date, playerName);
        return ResponseEntity.ok(history);
    }
}