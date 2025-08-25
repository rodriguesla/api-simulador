package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SimulacaoRequestDTO {
    @Schema(required = true, description = "Valor desejado para a simulação")
    private Double valorDesejado;
    @Schema(required = true, description = "Prazo da simulação em meses")
    private Integer prazo;

}
