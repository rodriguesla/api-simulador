package com.github.rodriguesla.api_simulador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ValorTotalParcelasDTO {
    private BigDecimal sac;
    private BigDecimal price;
}