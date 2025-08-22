package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.EndpointTelemetriaDTO;
import com.github.rodriguesla.api_simulador.dto.TelemetriaResponseDTO;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulacaoTelemetriaServiceTest {

    @Test
    void getTelemetriaReturnsCorrectMetrics() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        Timer timer = mock(Timer.class);
        io.micrometer.core.instrument.search.Search search = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(search);
        when(search.tag("uri", "/simulacoes")).thenReturn(search);
        when(search.timer()).thenReturn(timer);

        when(timer.count()).thenReturn(10L);
        when(timer.mean(TimeUnit.MILLISECONDS)).thenReturn(100.0);
        when(timer.max(TimeUnit.MILLISECONDS)).thenReturn(200.0);

        when(search.meters()).thenReturn(Collections.singletonList(timer));
        io.micrometer.core.instrument.Meter.Id meterId = mock(io.micrometer.core.instrument.Meter.Id.class);
        when(timer.getId()).thenReturn(meterId);
        when(meterId.getTag("status")).thenReturn("200");

        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        when(simulacaoCalculoService.getMinElapsedTime()).thenReturn(50L);
        SimulacaoTelemetriaService service = new SimulacaoTelemetriaService(simulacaoCalculoService);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals("API Simulador", endpoint.getNomeApi());
        assertEquals(10, endpoint.getQtdRequisicoes());
        assertEquals(100, endpoint.getTempoMedio());
        assertEquals(50, endpoint.getTempoMinimo());
        assertEquals(200, endpoint.getTempoMaximo());
        assertTrue(endpoint.getPercentualSucesso() > 0.0);
    }

    @Test
    void getTelemetriaHandlesNoTimer() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        io.micrometer.core.instrument.search.Search search = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(search);
        when(search.tag("uri", "/simulacoes")).thenReturn(search);
        when(search.timer()).thenReturn(null);
        when(search.meters()).thenReturn(Collections.emptyList());

        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        when(simulacaoCalculoService.getMinElapsedTime()).thenReturn(50L);
        SimulacaoTelemetriaService service = new SimulacaoTelemetriaService(simulacaoCalculoService);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals(0, endpoint.getQtdRequisicoes());
        assertEquals(0, endpoint.getTempoMedio());
        assertEquals(50, endpoint.getTempoMinimo());
        assertEquals(0, endpoint.getTempoMaximo());
        assertEquals(0.0, endpoint.getPercentualSucesso());
    }
}