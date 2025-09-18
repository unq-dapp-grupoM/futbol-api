package com.dapp.futbol_api.model.dto;

import lombok.Data;

import java.util.List;

@Data // La anotación de Lombok que genera getters, setters, toString, etc.
public class PlayerDTO {
    private String name;
    private String shirtNumber;
    private String age;
    private String height;
    private String positions;
    private String nationality;
    private String currentTeam;
    private List<PlayerMatchStatsDTO> matchStats;
}