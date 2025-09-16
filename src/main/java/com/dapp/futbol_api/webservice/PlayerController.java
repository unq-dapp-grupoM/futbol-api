package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.service.PlayerService;

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
@RequestMapping("/api/searchPlayer")
@Tag(name = "Info de Jugadores", description = "Endpoints para obtener información de jugadores.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;

  @Operation(summary = "Buscar y obtener información de un jugador por nombre", description = "Busca un jugador por su nombre en WhoScored y extrae sus detalles. ¡REQUIERE AUTENTICACIÓN!")
  @GetMapping("/playerName")
  public ResponseEntity<?> getPlayerInfoByName(
      @Parameter(description = "Nombre del jugador a buscar.", example = "Lionel Messi") @RequestParam("playerName") String playerName) {
    try {
      PlayerDTO player = playerService.getPlayerInfoByName(playerName);
      return ResponseEntity.ok(player);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error al buscar o procesar la información del jugador '" + playerName + "': " + e.getMessage());
    }
  }
}
