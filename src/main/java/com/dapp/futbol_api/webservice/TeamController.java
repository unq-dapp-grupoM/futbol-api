package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Team Info", description = "Endpoints for team information.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TeamController {

    private static final String NEW_LINE_REGEX = "[\n\r]";
    private final TeamService teamService;

    @Operation(summary = "Search and get team info", description = "Find a team and your players. AUTHENTICATION REQUIRED!")
    @GetMapping("/team")
    public ResponseEntity<Object> getTeamInfoByName(
            @Parameter(description = "Name of the team to search for.", example = "Real Madrid") @RequestParam("teamName") String teamName) {
        final String sanitizedTeamName = sanitize(teamName);
        Object team = teamService.getTeamInfoByName(sanitizedTeamName);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Get future matches for a team", description = "Get a list of future matches for a given team. AUTHENTICATION REQUIRED!")
    @GetMapping("/futureMatches")
    public ResponseEntity<Object> getFutureMatches(
            @Parameter(description = "Name of the team to search for.", example = "Real Madrid") @RequestParam("teamName") String teamName) {
        final String sanitizedTeamName = sanitize(teamName);
        Object matches = teamService.getFutureMatches(sanitizedTeamName);
        return ResponseEntity.ok(matches);
    }

    private String sanitize(String input) {
        return input.replaceAll(NEW_LINE_REGEX, "_");
    }
}
