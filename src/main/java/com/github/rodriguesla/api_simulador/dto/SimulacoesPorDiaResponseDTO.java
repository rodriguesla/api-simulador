package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulacoesPorDiaResponseDTO {
    private String dataReferencia;
    private List<SimulacaoPorDiaProdutoDTO> simulacoes;
}