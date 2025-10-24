package com.dapp.futbol_api.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchDTO {
    private String homeTeam;
    private String awayTeam;
    private String date;
    private String competition;
}
