package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(required = true, description = "Número da parcela")
    private Integer numero;
    @JsonProperty("valorAmortizacao")
    @Schema(required = true, description = "Valor da amortização da parcela")
    private Double valorAmortizacao;
    @JsonProperty("valorJuros")
    @Schema(required = true, description = "Valor dos juros da parcela")
    private Double valorJuros;
    @JsonProperty("valorPrestacao")
    @Schema(required = true, description = "Valor total da prestação da parcela")
    private Double valorPrestacao;
}