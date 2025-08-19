package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelaDTO {
    @JsonProperty("numero")
    private Integer numero;
    @JsonProperty("valorAmortizacao")
    private Double valorAmortizacao;
    @JsonProperty("valorJuros")
    private Double valorJuros;
    @JsonProperty("valorPrestacao")
    private Double valorPrestacao;
}