package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.service.SimulacaoViewService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SimulacaoViewControllerTest {
    private static final String VIEW_SIMULACOES = "simulacoes";
    private static final String VIEW_SIMULACOES_POR_DIA = "simulacoes_por_dia";
    private static final String VIEW_TELEMETRIA = "telemetria";
    private static final String VIEW_HOME = "home";
    private static final String TEST_DATE_2025_08_24 = "2025-08-24";

    private SimulacaoViewService simulacaoViewService;
    private SimulacaoViewController controller;

    @BeforeEach
    void setUp() {
        simulacaoViewService = mock(SimulacaoViewService.class);
        controller = new SimulacaoViewController(simulacaoViewService);
    }

    @Test
    void home_returnsHomeView() {
        String view = controller.home();
        assertEquals(VIEW_HOME, view);
    }

    @Test
    void listarSimulacoes_delegatesToService() {
        Model model = mock(Model.class);
        when(simulacaoViewService.listarSimulacoes(model)).thenReturn(VIEW_SIMULACOES);
        String view = controller.listarSimulacoes(model);
        verify(simulacaoViewService).listarSimulacoes(model);
        assertEquals(VIEW_SIMULACOES, view);
    }

    @Test
    void listarSimulacoesPorDia_delegatesToService() {
        Model model = mock(Model.class);
        when(simulacaoViewService.listarSimulacoesPorDia(TEST_DATE_2025_08_24, model)).thenReturn(VIEW_SIMULACOES_POR_DIA);
        String view = controller.listarSimulacoesPorDia(TEST_DATE_2025_08_24, model);
        verify(simulacaoViewService).listarSimulacoesPorDia(TEST_DATE_2025_08_24, model);
        assertEquals(VIEW_SIMULACOES_POR_DIA, view);
    }

    @Test
    void mostrarTelemetria_delegatesToService() {
        Model model = mock(Model.class);
        when(simulacaoViewService.mostrarTelemetria(model)).thenReturn(VIEW_TELEMETRIA);
        String view = controller.mostrarTelemetria(model);
        verify(simulacaoViewService).mostrarTelemetria(model);
        assertEquals(VIEW_TELEMETRIA, view);
    }
}
