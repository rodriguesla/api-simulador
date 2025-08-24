package com.github.rodriguesla.api_simulador.dto;

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
    private String dataReferencia;
    private List<EndpointTelemetriaDTO> listaEndpoints;
}