package com.github.rodriguesla.api_simulador.dto;

import lombok.*;

import java.util.List;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacoesPorDiaResponseDTO {
    private String dataReferencia;
    private List<SimulacaoPorDiaProdutoDTO> simulacoes;
}