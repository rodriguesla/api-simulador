package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.service.SimulacaoListaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoPorDiaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoTelemetriaService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SimulacaoViewControllerTest {
    private static final String VIEW_SIMULACOES = "simulacoes";
    private static final String VIEW_SIMULACOES_POR_DIA = "simulacoes_por_dia";
    private static final String VIEW_TELEMETRIA = "telemetria";
    private static final String ATTR_SIMULACOES = "simulacoes";
    private static final String ATTR_DATA_REFERENCIA = "dataReferencia";
    private static final String ATTR_SIMULACOES_POR_DIA = "simulacoesPorDia";
    private static final String ATTR_TELEMETRIA = "telemetria";
    private static final String TEST_DATE_2025_08_24 = "2025-08-24";
    private static final String TEST_DATE_2025_08_23 = "2025-08-23";

    private SimulacaoListaService simulacaoListaService;
    private SimulacaoPorDiaService simulacaoPorDiaService;
    private SimulacaoTelemetriaService simulacaoTelemetriaService;
    private MeterRegistry meterRegistry;
    private Model model;
    private SimulacaoViewController controller;

    @BeforeEach
    void setUp() {
        simulacaoListaService = mock(SimulacaoListaService.class);
        simulacaoPorDiaService = mock(SimulacaoPorDiaService.class);
        simulacaoTelemetriaService = mock(SimulacaoTelemetriaService.class);
        meterRegistry = mock(MeterRegistry.class);
        model = mock(Model.class);
        controller = new SimulacaoViewController(simulacaoListaService, simulacaoPorDiaService, simulacaoTelemetriaService, meterRegistry);
    }

    @Test
    void listarSimulacoes_addsSimulacoesToModelAndReturnsView() {
        SimulacaoListaResponseDTO responseDTO = new SimulacaoListaResponseDTO();
        responseDTO.setRegistros(Arrays.asList(
            SimulacaoRegistroDTO.builder()
                .idSimulacao("sim1")
                .prazo(10)
                .valorDesejado(new java.math.BigDecimal("100.00"))
                .valorTotalParcelas(new java.math.BigDecimal("120.00"))
                .valorSacPrice(ValorSacPriceDTO.builder()
                    .sac(new java.math.BigDecimal("119.00"))
                    .price(new java.math.BigDecimal("121.00"))
                    .build())
                .build(),
            SimulacaoRegistroDTO.builder()
                .idSimulacao("sim2")
                .prazo(12)
                .valorDesejado(new java.math.BigDecimal("200.00"))
                .valorTotalParcelas(new java.math.BigDecimal("240.00"))
                .valorSacPrice(ValorSacPriceDTO.builder()
                    .sac(new java.math.BigDecimal("239.00"))
                    .price(new java.math.BigDecimal("241.00"))
                    .build())
                .build()
        ));
        when(simulacaoListaService.buscarSimulacoesLista(0, 200)).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoes(model);

        verify(simulacaoListaService).buscarSimulacoesLista(0, 200);
        verify(model).addAttribute(eq(ATTR_SIMULACOES), captor.capture());
        assertEquals(VIEW_SIMULACOES, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(2, formattedList.size());
        for (Object item : formattedList) {
            assert item instanceof java.util.Map;
            java.util.Map<?,?> map = (java.util.Map<?,?>) item;
            assert map.containsKey("idSimulacao");
            assert map.containsKey("prazo");
            assert map.containsKey("valorDesejado");
            assert map.containsKey("valorTotalParcelas");
            assert map.containsKey("valorSacPriceSac");
            assert map.containsKey("valorSacPricePrice");
        }
    }

    @Test
    void listarSimulacoesPorDia_withNullData_usesCurrentDate() {
        SimulacoesPorDiaResponseDTO responseDTO = new SimulacoesPorDiaResponseDTO();
        responseDTO.setDataReferencia(TEST_DATE_2025_08_24);
        responseDTO.setSimulacoes(Collections.emptyList());
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoesPorDia(null, model);

        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(anyString());
        verify(model).addAttribute(ATTR_DATA_REFERENCIA, TEST_DATE_2025_08_24);
        verify(model).addAttribute(eq(ATTR_SIMULACOES_POR_DIA), captor.capture());
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(0, formattedList.size());
    }

    @Test
    void listarSimulacoesPorDia_withBlankData_usesCurrentDate() {
        SimulacoesPorDiaResponseDTO responseDTO = new SimulacoesPorDiaResponseDTO();
        responseDTO.setDataReferencia(TEST_DATE_2025_08_24);
        responseDTO.setSimulacoes(Collections.emptyList());
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(anyString())).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoesPorDia("   ", model);

        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(anyString());
        verify(model).addAttribute(ATTR_DATA_REFERENCIA, TEST_DATE_2025_08_24);
        verify(model).addAttribute(eq(ATTR_SIMULACOES_POR_DIA), captor.capture());
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(0, formattedList.size());
    }

    @Test
    void listarSimulacoesPorDia_withData_callsServiceWithData() {
        SimulacoesPorDiaResponseDTO responseDTO = new SimulacoesPorDiaResponseDTO();
        responseDTO.setDataReferencia(TEST_DATE_2025_08_23);
        responseDTO.setSimulacoes(Collections.emptyList());
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(TEST_DATE_2025_08_23)).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoesPorDia(TEST_DATE_2025_08_23, model);

        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(TEST_DATE_2025_08_23);
        verify(model).addAttribute(ATTR_DATA_REFERENCIA, TEST_DATE_2025_08_23);
        verify(model).addAttribute(eq(ATTR_SIMULACOES_POR_DIA), captor.capture());
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(0, formattedList.size());
    }

    @Test
    void listarSimulacoesPorDia_withSimulacoes_formatsDataCorrectly() {
        SimulacoesPorDiaResponseDTO responseDTO = new SimulacoesPorDiaResponseDTO();
        responseDTO.setDataReferencia(TEST_DATE_2025_08_24);
        responseDTO.setSimulacoes(Arrays.asList(
            SimulacaoPorDiaProdutoDTO.builder()
                .codigoProduto(1L)
                .descricaoProduto("Produto Test")
                .taxaMediaJuro(new java.math.BigDecimal("5.50"))
                .valorMedioPrestacao(new java.math.BigDecimal("1500.75"))
                .valorTotalDesejado(new java.math.BigDecimal("50000.00"))
                .valorTotalCredito(new java.math.BigDecimal("55000.00"))
                .build(),
            SimulacaoPorDiaProdutoDTO.builder()
                .codigoProduto(2L)
                .descricaoProduto("Produto Test 2")
                .taxaMediaJuro(new java.math.BigDecimal("6.25"))
                .valorMedioPrestacao(new java.math.BigDecimal("2000.50"))
                .valorTotalDesejado(new java.math.BigDecimal("75000.00"))
                .valorTotalCredito(new java.math.BigDecimal("82500.00"))
                .build()
        ));
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(TEST_DATE_2025_08_24)).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoesPorDia(TEST_DATE_2025_08_24, model);

        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(TEST_DATE_2025_08_24);
        verify(model).addAttribute(ATTR_DATA_REFERENCIA, TEST_DATE_2025_08_24);
        verify(model).addAttribute(eq(ATTR_SIMULACOES_POR_DIA), captor.capture());
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(2, formattedList.size());

        for (Object item : formattedList) {
            assert item instanceof java.util.Map;
            java.util.Map<?,?> map = (java.util.Map<?,?>) item;
            assert map.containsKey("codigoProduto");
            assert map.containsKey("descricaoProduto");
            assert map.containsKey("taxaMediaJuro");
            assert map.containsKey("valorMedioPrestacao");
            assert map.containsKey("valorTotalDesejado");
            assert map.containsKey("valorTotalCredito");
        }
    }

    @Test
    void listarSimulacoesPorDia_withNullValues_handlesNullsCorrectly() {
        SimulacoesPorDiaResponseDTO responseDTO = new SimulacoesPorDiaResponseDTO();
        responseDTO.setDataReferencia(TEST_DATE_2025_08_24);
        responseDTO.setSimulacoes(Arrays.asList(
            SimulacaoPorDiaProdutoDTO.builder()
                .codigoProduto(1L)
                .descricaoProduto("Produto Test")
                .taxaMediaJuro(null)
                .valorMedioPrestacao(null)
                .valorTotalDesejado(null)
                .valorTotalCredito(null)
                .build()
        ));
        when(simulacaoPorDiaService.buscarSimulacoesPorDia(TEST_DATE_2025_08_24)).thenReturn(responseDTO);

        org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        String view = controller.listarSimulacoesPorDia(TEST_DATE_2025_08_24, model);

        verify(simulacaoPorDiaService).buscarSimulacoesPorDia(TEST_DATE_2025_08_24);
        verify(model).addAttribute(ATTR_DATA_REFERENCIA, TEST_DATE_2025_08_24);
        verify(model).addAttribute(eq(ATTR_SIMULACOES_POR_DIA), captor.capture());
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);

        Object value = captor.getValue();
        assert value instanceof java.util.List;
        java.util.List<?> formattedList = (java.util.List<?>) value;
        assertEquals(1, formattedList.size());

        java.util.Map<?,?> map = (java.util.Map<?,?>) formattedList.get(0);
        assertEquals("", map.get("taxaMediaJuro"));
        assertEquals("", map.get("valorMedioPrestacao"));
        assertEquals("", map.get("valorTotalDesejado"));
        assertEquals("", map.get("valorTotalCredito"));
    }

    @Test
    void mostrarTelemetria_addsTelemetriaToModelAndReturnsView() {
        TelemetriaResponseDTO telemetria = new TelemetriaResponseDTO();
        when(simulacaoTelemetriaService.getTelemetria(meterRegistry)).thenReturn(telemetria);

        String view = controller.mostrarTelemetria(model);

        verify(simulacaoTelemetriaService).getTelemetria(meterRegistry);
        verify(model).addAttribute(ATTR_TELEMETRIA, telemetria);
        assertEquals(VIEW_TELEMETRIA, view);
    }
}
