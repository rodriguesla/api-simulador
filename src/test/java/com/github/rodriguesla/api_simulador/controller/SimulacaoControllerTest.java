package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.exception.RequisicaoInvalidaException;
import com.github.rodriguesla.api_simulador.service.SimulacaoCalculoService;
import com.github.rodriguesla.api_simulador.service.SimulacaoListaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoPorDiaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoTelemetriaService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SimulacaoControllerTest {

    @Test
    void testSimularReturnsOkWithResponses() {
        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        SimulacaoListaService simulacaoListaService = mock(SimulacaoListaService.class);
        SimulacaoPorDiaService simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        SimulacaoTelemetriaService simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(
                simulacaoCalculoService,
                simulacaoListaService,
                simulacaoPorDiaService,
                simulacaoTelemetriaService,
                meterRegistry
        );

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        SimulacaoResponseDTO responseDTO = new SimulacaoResponseDTO();
        List<SimulacaoResponseDTO> expectedResponses = Collections.singletonList(responseDTO);

        when(simulacaoCalculoService.simular(request)).thenReturn(expectedResponses);

        ResponseEntity<List<SimulacaoResponseDTO>> response = controller.simular(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponses, response.getBody());
        verify(simulacaoCalculoService, times(1)).simular(request);
    }

    @Test
    void testListarSimulacoesReturnsPaginatedDTO() {
        SimulacaoListaService simulacaoListaService = mock(SimulacaoListaService.class);
        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        SimulacaoPorDiaService simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        SimulacaoTelemetriaService simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(
                simulacaoCalculoService,
                simulacaoListaService,
                simulacaoPorDiaService,
                simulacaoTelemetriaService,
                meterRegistry
        );

        int pagina = 2;
        int qtdRegistrosPagina = 5;

        SimulacaoListaResponseDTO mockResponse = SimulacaoListaResponseDTO.builder()
                .pagina(pagina)
                .qtdRegistros(10)
                .qtdRegistrosPagina(qtdRegistrosPagina)
                .registros(Collections.emptyList())
                .build();

        when(simulacaoListaService.buscarSimulacoesLista(pagina, qtdRegistrosPagina)).thenReturn(mockResponse);

        SimulacaoListaResponseDTO response = controller.listarSimulacoes(pagina, qtdRegistrosPagina);

        assertEquals(pagina, response.getPagina());
        assertEquals(10, response.getQtdRegistros());
        assertEquals(qtdRegistrosPagina, response.getQtdRegistrosPagina());
        assertTrue(response.getRegistros().isEmpty());
        verify(simulacaoListaService, times(1)).buscarSimulacoesLista(pagina, qtdRegistrosPagina);
    }

    @Test
    void testSimulacoesPorDiaReturnsAggregatedDTO() {
        SimulacaoPorDiaService simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        SimulacaoListaService simulacaoListaService = mock(SimulacaoListaService.class);
        SimulacaoTelemetriaService simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(
                simulacaoCalculoService,
                simulacaoListaService,
                simulacaoPorDiaService,
                simulacaoTelemetriaService,
                meterRegistry
        );

        String data = "2024-06-10";
        SimulacoesPorDiaResponseDTO mockResponse = SimulacoesPorDiaResponseDTO.builder()
                .dataReferencia(data)
                .simulacoes(Collections.emptyList())
                .build();

        when(simulacaoPorDiaService.buscarSimulacoesPorDia(data)).thenReturn(mockResponse);

        SimulacoesPorDiaResponseDTO response = controller.simulacoesPorDia(data);

        assertEquals(data, response.getDataReferencia());
        assertTrue(response.getSimulacoes().isEmpty());
        verify(simulacaoPorDiaService, times(1)).buscarSimulacoesPorDia(data);
    }

    @Test
    void testTelemetriaReturnsOkWithTelemetryDTO() {
        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        SimulacaoListaService simulacaoListaService = mock(SimulacaoListaService.class);
        SimulacaoPorDiaService simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        SimulacaoTelemetriaService simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(
                simulacaoCalculoService,
                simulacaoListaService,
                simulacaoPorDiaService,
                simulacaoTelemetriaService,
                meterRegistry
        );

        EndpointTelemetriaDTO endpointTelemetry = EndpointTelemetriaDTO.builder()
                .nomeApi("simulacao")
                .qtdRequisicoes(123)
                .tempoMedio(456L)
                .tempoMinimo(100L)
                .tempoMaximo(900L)
                .percentualSucesso(99.5)
                .build();

        TelemetriaResponseDTO telemetryResponse = TelemetriaResponseDTO.builder()
                .dataReferencia("2024-06-10")
                .listaEndpoints(List.of(endpointTelemetry))
                .build();

        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenReturn(telemetryResponse);

        ResponseEntity<TelemetriaResponseDTO> response = controller.telemetria(Collections.emptyMap());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(telemetryResponse, response.getBody());
        verify(simulacaoTelemetriaService, times(1)).getTelemetria(meterRegistry);
    }

    @Test
    void testTelemetriaWithParamsThrowsBadRequest() {
        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        SimulacaoListaService simulacaoListaService = mock(SimulacaoListaService.class);
        SimulacaoPorDiaService simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        SimulacaoTelemetriaService simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(
                simulacaoCalculoService,
                simulacaoListaService,
                simulacaoPorDiaService,
                simulacaoTelemetriaService,
                meterRegistry
        );

        Map<String, String> params = Map.of("foo", "bar");
        assertThrows(RequisicaoInvalidaException.class, () -> controller.telemetria(params));
    }
}