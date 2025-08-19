package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String tipo;
    @JsonProperty("parcelas")
    private List<ParcelaDTO> parcelas;
}