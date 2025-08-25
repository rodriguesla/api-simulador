package com.github.rodriguesla.api_simulador.dto;

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
public class TelemetriaResponseDTO {
    @Schema(required = false, description = "Data de referência da telemetria")
    private String dataReferencia;
    @Schema(required = false, description = "Lista de endpoints monitorados")
    private List<EndpointTelemetriaDTO> listaEndpoints;
}