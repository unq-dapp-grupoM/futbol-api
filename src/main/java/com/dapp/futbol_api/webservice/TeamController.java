package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.model.dto.TeamDTO;
import com.dapp.futbol_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teamInfo")
@Tag(name = "Team Info", description = "Endpoints for team information.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Search and get team info", description = "Find a team and your players. AUTHENTICATION REQUIRED!")
    @GetMapping("/teamName")
    public ResponseEntity<?> getTeamInfoByName(
            @Parameter(description = "Name of the team to search for.", example = "Real Madrid") @RequestParam("teamName") String teamName) {
        try {
            TeamDTO team = teamService.getTeamInfoByName(teamName);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching or processing team information for '" + teamName + "': " + e.getMessage());
        }
    }
}
