package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulacaoListaResponseDTO {
    private int pagina;
    private long qtdRegistros;
    private int qtdRegistrosPagina;
    private List<SimulacaoRegistroDTO> registros;
}