package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointTelemetriaDTO {
    @Schema(required = true, description = "Nome da API monitorada")
    private String nomeApi;
    @Schema(required = true, description = "Quantidade de requisições recebidas")
    private int qtdRequisicoes;
    @Schema(required = true, description = "Tempo médio de resposta (ms)")
    private long tempoMedio;
    @Schema(required = true, description = "Tempo mínimo de resposta (ms)")
    private long tempoMinimo;
    @Schema(required = true, description = "Tempo máximo de resposta (ms)")
    private long tempoMaximo;
    @Schema(required = true, description = "Percentual de sucesso das requisições")
    private double percentualSucesso;
}