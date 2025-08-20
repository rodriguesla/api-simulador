package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointTelemetriaDTO {
    private String nomeApi;
    private int qtdRequisicoes;
    private long tempoMedio;
    private long tempoMinimo;
    private long tempoMaximo;
    private double percentualSucesso;
}