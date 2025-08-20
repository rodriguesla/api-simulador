package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.service.SimulacaoService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SimulacaoControllerTest {

    @Test
    void testSimularReturnsOkWithResponses() {
        SimulacaoService simulacaoService = mock(SimulacaoService.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(simulacaoService, simulacaoRepository, meterRegistry);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO(); // Fill with sample data if needed
        SimulacaoResponseDTO responseDTO = new SimulacaoResponseDTO(); // Fill with sample data if needed
        List<SimulacaoResponseDTO> expectedResponses = Collections.singletonList(responseDTO);

        when(simulacaoService.simular(request)).thenReturn(expectedResponses);

        ResponseEntity<?> response = controller.simular(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponses, response.getBody());
        verify(simulacaoService, times(1)).simular(request);
    }

    @Test
    void testListarSimulacoesReturnsPaginatedDTO() {
        SimulacaoService simulacaoService = mock(SimulacaoService.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(simulacaoService, simulacaoRepository, meterRegistry);

        int pagina = 2;
        int qtdRegistrosPagina = 5;

        Simulacao simulacao = Simulacao.builder()
                .id(1L)
                .produtoId(100)
                .valorDesejado(5000.0)
                .prazo(24)
                .resultadoSac(5200.0)
                .resultadoPrice(5300.0)
                .dataSimulacao(LocalDateTime.of(2024, 6, 10, 12, 0))
                .envelopeJson("{}")
                .build();

        List<Simulacao> simulacoes = Collections.singletonList(simulacao);
        when(simulacaoService.buscarSimulacoesPaginadas(pagina, qtdRegistrosPagina)).thenReturn(simulacoes);
        when(simulacaoRepository.count()).thenReturn(10L);

        SimulacaoListaResponseDTO result = controller.listarSimulacoes(pagina, qtdRegistrosPagina);

        assertEquals(pagina, result.getPagina());
        assertEquals(10, result.getQtdRegistros());
        assertEquals(qtdRegistrosPagina, result.getQtdRegistrosPagina());
        assertEquals(1, result.getRegistros().size());

        SimulacaoRegistroDTO dto = result.getRegistros().get(0);
        assertEquals("20240610", dto.getIdSimulacao());
        assertEquals(simulacao.getPrazo(), dto.getPrazo());
        assertEquals(simulacao.getValorDesejado(), dto.getValorDesejado().doubleValue());
    }

    @Test
    void testSimulacoesPorDiaReturnsAggregatedDTO() {
        SimulacaoService simulacaoService = mock(SimulacaoService.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(simulacaoService, simulacaoRepository, meterRegistry);

        String data = "2024-06-10";
        LocalDate dia = LocalDate.parse(data);
        LocalDateTime start = dia.atStartOfDay();
        LocalDateTime end = dia.atTime(LocalTime.MAX);

        Simulacao simulacao = Simulacao.builder()
                .id(1L)
                .produtoId(100)
                .valorDesejado(5000.0)
                .prazo(24)
                .resultadoSac(5200.0)
                .resultadoPrice(5300.0)
                .dataSimulacao(start.plusHours(10))
                .envelopeJson("{}")
                .build();

        Produto produto = Produto.builder()
                .id(100)
                .nome("Produto Teste")
                .taxaJuros(BigDecimal.valueOf(0.05))
                .prazoMinimo((short)12)
                .prazoMaximo((short)48)
                .valorMinimo(BigDecimal.valueOf(1000))
                .valorMaximo(BigDecimal.valueOf(10000))
                .build();

        when(simulacaoRepository.findByDataSimulacaoBetween(start, end)).thenReturn(Collections.singletonList(simulacao));
        when(simulacaoService.buscarTodosProdutos()).thenReturn(Collections.singletonList(produto));

        SimulacoesPorDiaResponseDTO response = controller.simulacoesPorDia(data);

        assertEquals(data, response.getDataReferencia());
        assertEquals(1, response.getSimulacoes().size());

        SimulacaoPorDiaProdutoDTO dto = response.getSimulacoes().get(0);
        assertEquals(100L, dto.getCodigoProduto());
        assertEquals("Produto Teste", dto.getDescricaoProduto());
        assertEquals(BigDecimal.valueOf(0.05), dto.getTaxaMediaJuro());
        assertEquals(BigDecimal.valueOf((5200.0 + 5300.0) / 2), dto.getValorMedioPrestacao());
        assertEquals(BigDecimal.valueOf(5000.0), dto.getValorTotalDesejado());
        assertEquals(BigDecimal.valueOf(5200.0 + 5300.0), dto.getValorTotalCredito());
    }

    @Test
    void testTelemetriaReturnsOkWithTelemetryDTO() {
        SimulacaoService simulacaoService = mock(SimulacaoService.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        SimulacaoController controller = new SimulacaoController(simulacaoService, simulacaoRepository, meterRegistry);

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

        when(simulacaoService.getTelemetria(meterRegistry)).thenReturn(telemetryResponse);

        ResponseEntity<TelemetriaResponseDTO> response = controller.telemetria();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(telemetryResponse, response.getBody());
        verify(simulacaoService, times(1)).getTelemetria(meterRegistry);
    }
}