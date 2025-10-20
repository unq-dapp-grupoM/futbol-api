package com.dapp.futbol_api.model.dto;

import lombok.Data;

@Data
public class AnalysisRequestDTO {
    private String playerName;
    private String opponent;
    private Boolean isHome;
    private String position;

    public AnalysisRequestDTO() {
    }

    public AnalysisRequestDTO(String playerName, String opponent, Boolean isHome, String position) {
        this.playerName = playerName;
        this.opponent = opponent;
        this.isHome = isHome;
        this.position = position;
    }
}