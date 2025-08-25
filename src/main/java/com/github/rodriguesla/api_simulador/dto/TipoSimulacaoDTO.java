package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TipoSimulacaoDTO {
    @JsonProperty("tipo")
    @Schema(required = true, description = "Tipo de simulação (SAC ou PRICE)")
    private String tipo;
    @JsonProperty("parcelas")
    @Schema(required = true, description = "Lista de parcelas para o tipo de simulação")
    private List<ParcelaDTO> parcelas;
}