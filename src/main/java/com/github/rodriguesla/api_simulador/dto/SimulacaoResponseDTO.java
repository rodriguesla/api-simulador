package com.github.rodriguesla.api_simulador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulacaoResponseDTO {
    @JsonProperty("idSimulacao")
    private String idSimulacao;
    @JsonProperty("codigoProduto")
    private Integer codigoProduto;
    @JsonProperty("descricaoProduto")
    private String descricaoProduto;
    @JsonProperty("taxaJuros")
    private Double taxaJuros;
    @JsonProperty("resultadoSimulacao")
    private List<TipoSimulacaoDTO> resultadoSimulacao;

}
