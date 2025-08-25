package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SimulacaoRegistroDTO {
    @Schema(required = true, description = "Identificador da simulação")
    private String idSimulacao;
    @Schema(required = true, description = "Valor desejado para a simulação")
    private BigDecimal valorDesejado;
    @Schema(required = true, description = "Prazo da simulação em meses")
    private Integer prazo;
    @Schema(required = true, description = "Valor total das parcelas (média entre SAC e PRICE)")
    private BigDecimal valorTotalParcelas; // mean of sac and price
    @Schema(required = true, description = "Valores SAC e PRICE separados")
    private ValorSacPriceDTO valorSacPrice; // sac and price separated
}