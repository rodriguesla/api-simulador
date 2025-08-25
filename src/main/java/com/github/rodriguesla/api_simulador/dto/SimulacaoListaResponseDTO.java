package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoListaResponseDTO {
    @Schema(required = true, description = "Número da página atual")
    private int pagina;
    @Schema(required = true, description = "Quantidade total de registros")
    private long qtdRegistros;
    @Schema(required = true, description = "Quantidade de registros por página")
    private int qtdRegistrosPagina;
    @Schema(required = true, description = "Lista de registros de simulação")
    private List<SimulacaoRegistroDTO> registros;
}