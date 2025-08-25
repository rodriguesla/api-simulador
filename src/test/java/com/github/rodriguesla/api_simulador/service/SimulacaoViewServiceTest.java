package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.dto.EndpointTelemetriaDTO;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SimulacaoViewServiceTest {
    private SimulacaoListaService simulacaoListaService;
    private SimulacaoPorDiaService simulacaoPorDiaService;
    private SimulacaoTelemetriaService simulacaoTelemetriaService;
    private MeterRegistry meterRegistry;
    private Model model;
    private SimulacaoViewService service;

    @BeforeEach
    void setUp() {
        simulacaoListaService = mock(SimulacaoListaService.class);
        simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        meterRegistry = mock(MeterRegistry.class);
        model = mock(Model.class);
        service = new SimulacaoViewService(simulacaoListaService, simulacaoPorDiaService, simulacaoTelemetriaService, meterRegistry);
    }

    // listarSimulacoes tests
    @Test
    void listarSimulacoessuccess() {
        SimulacaoListaResponseDTO dto = new SimulacaoListaResponseDTO();
        dto.setRegistros(Arrays.asList(
                SimulacaoRegistroDTO.builder()
                        .idSimulacao("sim1").prazo(10)
                        .valorDesejado(new BigDecimal("100.00"))
                        .valorTotalParcelas(new BigDecimal("120.00"))
                        .valorSacPrice(ValorSacPriceDTO.builder().sac(new BigDecimal("119.00")).price(new BigDecimal("121.00")).build())
                        .build()
        ));
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenReturn(dto);
        String view = service.listarSimulacoes(model);
        verify(model).addAttribute(eq("simulacoes"), any());
        assertEquals("simulacoes", view);
    }

    @Test
    void listarSimulacoesemptyList_returnsErro404() {
        SimulacaoListaResponseDTO dto = new SimulacaoListaResponseDTO();
        dto.setRegistros(Collections.emptyList());
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenReturn(dto);
        String view = service.listarSimulacoes(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada no sistema.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesnullList_returnsErro404() {
        SimulacaoListaResponseDTO dto = new SimulacaoListaResponseDTO();
        dto.setRegistros(null);
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenReturn(dto);
        String view = service.listarSimulacoes(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada no sistema.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesexceptionNoDataFound_returnsErro404() {
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenThrow(new RuntimeException("Nenhuma simulação encontrada"));
        String view = service.listarSimulacoes(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada no sistema.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesgenericException_returnsErro500() {
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenThrow(new RuntimeException("Database error"));
        String view = service.listarSimulacoes(model);
        verify(model).addAttribute("erro", "Erro Interno");
        verify(model).addAttribute("mensagem", "Ocorreu um erro ao buscar as simulações: Database error");
        verify(model).addAttribute("codigo", 500);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    // listarSimulacoesPorDia tests
    @Test
    void listarSimulacoesPorDia_withNullData_usesCurrentDate() {
        SimulacoesPorDiaResponseDTO dto = new SimulacoesPorDiaResponseDTO();
        dto.setDataReferencia(java.time.LocalDate.now().toString());
        dto.setSimulacoes(Arrays.asList(
                SimulacaoPorDiaProdutoDTO.builder().codigoProduto(1L).descricaoProduto("Produto").taxaMediaJuro(new BigDecimal("5.5")).valorMedioPrestacao(new BigDecimal("100.00")).valorTotalDesejado(new BigDecimal("1000.00")).valorTotalCredito(new BigDecimal("1100.00")).build()
        ));
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(java.time.LocalDate.now().toString())).thenReturn(dto);
        String view = service.listarSimulacoesPorDia(null, model);
        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(java.time.LocalDate.now().toString());
        verify(model).addAttribute("dataReferencia", java.time.LocalDate.now().toString());
        verify(model).addAttribute(eq("simulacoesPorDia"), any());
        assertEquals("simulacoes_por_dia", view);
    }

    @Test
    void listarSimulacoesPorDia_withBlankData_usesCurrentDate() {
        SimulacoesPorDiaResponseDTO dto = new SimulacoesPorDiaResponseDTO();
        dto.setDataReferencia(java.time.LocalDate.now().toString());
        dto.setSimulacoes(Arrays.asList(
                SimulacaoPorDiaProdutoDTO.builder().codigoProduto(1L).descricaoProduto("Produto").taxaMediaJuro(new BigDecimal("5.5")).valorMedioPrestacao(new BigDecimal("100.00")).valorTotalDesejado(new BigDecimal("1000.00")).valorTotalCredito(new BigDecimal("1100.00")).build()
        ));
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(java.time.LocalDate.now().toString())).thenReturn(dto);
        String view = service.listarSimulacoesPorDia("  ", model);
        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(java.time.LocalDate.now().toString());
        verify(model).addAttribute("dataReferencia", java.time.LocalDate.now().toString());
        verify(model).addAttribute(eq("simulacoesPorDia"), any());
        assertEquals("simulacoes_por_dia", view);
    }

    @Test
    void listarSimulacoesPorDia_success() {
        SimulacoesPorDiaResponseDTO dto = new SimulacoesPorDiaResponseDTO();
        dto.setDataReferencia("2025-08-25");
        dto.setSimulacoes(Arrays.asList(
                SimulacaoPorDiaProdutoDTO.builder().codigoProduto(1L).descricaoProduto("Produto").taxaMediaJuro(new BigDecimal("5.5")).valorMedioPrestacao(new BigDecimal("100.00")).valorTotalDesejado(new BigDecimal("1000.00")).valorTotalCredito(new BigDecimal("1100.00")).build()
        ));
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenReturn(dto);
        String view = service.listarSimulacoesPorDia("2025-08-25", model);
        verify(model).addAttribute("dataReferencia", "2025-08-25");
        verify(model).addAttribute(eq("simulacoesPorDia"), any());
        assertEquals("simulacoes_por_dia", view);
    }

    @Test
    void listarSimulacoesPorDiaemptyList_returnsErro404() {
        SimulacoesPorDiaResponseDTO dto = new SimulacoesPorDiaResponseDTO();
        dto.setDataReferencia("2025-08-25");
        dto.setSimulacoes(Collections.emptyList());
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenReturn(dto);
        String view = service.listarSimulacoesPorDia("2025-08-25", model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada para a data: 2025-08-25");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesPorDianullList_returnsErro404() {
        SimulacoesPorDiaResponseDTO dto = new SimulacoesPorDiaResponseDTO();
        dto.setDataReferencia("2025-08-25");
        dto.setSimulacoes(null);
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenReturn(dto);
        String view = service.listarSimulacoesPorDia("2025-08-25", model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada para a data: 2025-08-25");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesPorDiaexceptionNoDataFoundreturnsErro404() {
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenThrow(new RuntimeException("Nenhuma simulação encontrada"));
        String view = service.listarSimulacoesPorDia("2025-08-25", model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada para a data: 2025-08-25");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void listarSimulacoesPorDiagenericExceptionreturnsErro500() {
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenThrow(new RuntimeException("Database error"));
        String view = service.listarSimulacoesPorDia("2025-08-25", model);
        verify(model).addAttribute("erro", "Erro Interno");
        verify(model).addAttribute("mensagem", "Ocorreu um erro ao buscar simulações por dia: Database error");
        verify(model).addAttribute("codigo", 500);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void mostrarTelemetriasuccess() {
        TelemetriaResponseDTO dto = new TelemetriaResponseDTO();
        dto.setListaEndpoints(Arrays.asList(
                EndpointTelemetriaDTO.builder()
                        .nomeApi("Test API")
                        .qtdRequisicoes(10)
                        .tempoMedio(100L)
                        .tempoMinimo(80L)
                        .tempoMaximo(120L)
                        .percentualSucesso(99.5)
                        .build()
        ));
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenReturn(dto);
        String view = service.mostrarTelemetria(model);
        verify(model).addAttribute("telemetria", dto);
        assertEquals("telemetria", view);
    }

    @Test
    void mostrarTelemetria_emptyListreturnsErro404() {
        TelemetriaResponseDTO dto = new TelemetriaResponseDTO();
        dto.setListaEndpoints(Collections.emptyList());
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenReturn(dto);
        String view = service.mostrarTelemetria(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhum dado de telemetria foi encontrado.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void mostrarTelemetria_nullListreturnsErro404() {
        TelemetriaResponseDTO dto = new TelemetriaResponseDTO();
        dto.setListaEndpoints(null);
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenReturn(dto);
        String view = service.mostrarTelemetria(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhum dado de telemetria foi encontrado.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void mostrarTelemetriaexceptionNoDataFoundreturnsErro404() {
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenThrow(new RuntimeException("Nenhuma simulação encontrada. Crie uma simulação para gerar dados de telemetria."));
        String view = service.mostrarTelemetria(model);
        verify(model).addAttribute("erro", "Nenhum Dado Encontrado");
        verify(model).addAttribute("mensagem", "Nenhuma simulação foi encontrada. Crie uma simulação para gerar dados de telemetria.");
        verify(model).addAttribute("codigo", 404);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }

    @Test
    void mostrarTelemetriagenericExceptionreturnsErro500() {
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenThrow(new RuntimeException("Database error"));
        String view = service.mostrarTelemetria(model);
        verify(model).addAttribute("erro", "Erro Interno");
        verify(model).addAttribute("mensagem", "Ocorreu um erro ao buscar a telemetria: Database error");
        verify(model).addAttribute("codigo", 500);
        verify(model).addAttribute(eq("timestamp"), any(String.class));
        assertEquals("erro", view);
    }
}
