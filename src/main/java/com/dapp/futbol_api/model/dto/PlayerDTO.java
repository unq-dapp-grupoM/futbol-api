package com.dapp.futbol_api.model.dto;

import lombok.Data;

@Data // La anotación de Lombok que genera getters, setters, toString, etc.
public class PlayerDTO {
    private String nombre;
    private String numeroDorsal;
    private String edad;
    private String altura;
    private String posiciones;
    private String nacionalidad;
    private String equipoActual;
}