package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacoesPorDiaResponseDTO {
    @Schema(required = true, description = "Data de referência das simulações")
    private String dataReferencia;
    @Schema(required = true, description = "Lista de simulações por produto no dia")
    private List<SimulacaoPorDiaProdutoDTO> simulacoes;
}