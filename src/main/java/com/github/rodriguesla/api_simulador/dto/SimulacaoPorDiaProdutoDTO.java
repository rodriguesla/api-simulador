package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SimulacaoPorDiaProdutoDTO {
    @Schema(required = true, description = "Código do produto")
    private Long codigoProduto;
    @Schema(required = true, description = "Descrição do produto")
    private String descricaoProduto;
    @Schema(required = true, description = "Taxa média de juros")
    private java.math.BigDecimal taxaMediaJuro;
    @Schema(required = true, description = "Valor médio da prestação")
    private java.math.BigDecimal valorMedioPrestacao;
    @Schema(required = true, description = "Valor total desejado")
    private java.math.BigDecimal valorTotalDesejado;
    @Schema(required = true, description = "Valor total de crédito")
    private java.math.BigDecimal valorTotalCredito;
}