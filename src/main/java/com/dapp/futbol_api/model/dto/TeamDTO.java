package com.dapp.futbol_api.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class TeamDTO {
    private String name;
    private List<GameMatchDTO> fixture;
    private List<TeamPlayerDTO> squad;
}
