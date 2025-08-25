package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
public class ValorSacPriceDTO {
    @Schema(required = true, description = "Valor calculado pelo método SAC")
    private BigDecimal sac;
    @Schema(required = true, description = "Valor calculado pelo método PRICE")
    private BigDecimal price;
}
