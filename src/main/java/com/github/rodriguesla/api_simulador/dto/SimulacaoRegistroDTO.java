package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SimulacaoRegistroDTO {
    private String idSimulacao;
    private BigDecimal valorDesejado;
    private Integer prazo;
    private BigDecimal valorTotalParcelas; // mean of sac and price
    private ValorSacPriceDTO valorSacPrice; // sac and price separated
}