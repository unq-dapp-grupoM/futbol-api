package com.dapp.futbol_api.webservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("API de Fútbol corriendo correctamente");
    }
}
