package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulacaoResponseDTO {
    @JsonProperty("idSimulacao")
    @Schema(required = true, description = "Identificador da simulação")
    private String idSimulacao;
    @JsonProperty("codigoProduto")
    @Schema(required = true, description = "Código do produto")
    private Integer codigoProduto;
    @JsonProperty("descricaoProduto")
    @Schema(required = true, description = "Descrição do produto")
    private String descricaoProduto;
    @JsonProperty("taxaJuros")
    @Schema(required = true, description = "Taxa de juros aplicada")
    private Double taxaJuros;
    @JsonProperty("resultadoSimulacao")
    @Schema(required = true, description = "Lista de resultados da simulação por tipo")
    private java.util.List<TipoSimulacaoDTO> resultadoSimulacao;

}
