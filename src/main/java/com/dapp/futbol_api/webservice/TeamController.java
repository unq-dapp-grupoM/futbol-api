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
@RequestMapping("/api/fixtureTeam")
@Tag(name = "Info de Equipos", description = "Endpoints para obtener información de equipos.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Buscar y obtener partidos jugados", description = "Busca un historial de partidos jugados por el equipo. ¡REQUIERE AUTENTICACIÓN!")
    @GetMapping("/teamName")
    public ResponseEntity<?> getTeamInfoByName(
            @Parameter(description = "Nombre del equipo a buscar.", example = "Real Madrid") @RequestParam String teamName) {
        try {
            TeamDTO team = teamService.getTeamInfoByName(teamName);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar o procesar la información del jugador '" + teamName + "': "
                            + e.getMessage());
        }
    }
}
