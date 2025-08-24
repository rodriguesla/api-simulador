package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.EndpointTelemetriaDTO;
import com.github.rodriguesla.api_simulador.dto.TelemetriaResponseDTO;
import com.github.rodriguesla.api_simulador.exception.SimulacaoNaoEncontradaException;
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
        Timer timerSimulacoes = mock(Timer.class);
        Timer timerPorDia = mock(Timer.class);
        io.micrometer.core.instrument.search.Search searchSimulacoes = mock(io.micrometer.core.instrument.search.Search.class);
        io.micrometer.core.instrument.search.Search searchPorDia = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.tag("uri", "/simulacoes")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.timer()).thenReturn(timerSimulacoes);
        when(searchSimulacoes.meters()).thenReturn(Collections.singletonList(timerSimulacoes));

        when(searchSimulacoes.tag("uri", "/simulacoes/por-dia")).thenReturn(searchPorDia);
        when(searchPorDia.timer()).thenReturn(timerPorDia);
        when(searchPorDia.meters()).thenReturn(Collections.singletonList(timerPorDia));

        when(timerSimulacoes.count()).thenReturn(6L);
        when(timerSimulacoes.mean(TimeUnit.MILLISECONDS)).thenReturn(80.0);
        when(timerSimulacoes.max(TimeUnit.MILLISECONDS)).thenReturn(150.0);

        when(timerPorDia.count()).thenReturn(4L);
        when(timerPorDia.mean(TimeUnit.MILLISECONDS)).thenReturn(130.0);
        when(timerPorDia.max(TimeUnit.MILLISECONDS)).thenReturn(200.0);

        io.micrometer.core.instrument.Meter.Id meterIdSimulacoes = mock(io.micrometer.core.instrument.Meter.Id.class);
        io.micrometer.core.instrument.Meter.Id meterIdPorDia = mock(io.micrometer.core.instrument.Meter.Id.class);
        when(timerSimulacoes.getId()).thenReturn(meterIdSimulacoes);
        when(timerPorDia.getId()).thenReturn(meterIdPorDia);
        when(meterIdSimulacoes.getTag("status")).thenReturn("200");
        when(meterIdPorDia.getTag("status")).thenReturn("200");

        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        when(simulacaoCalculoService.getMinElapsedTime()).thenReturn(50L);
        SimulacaoTelemetriaService service = new SimulacaoTelemetriaService(simulacaoCalculoService);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals("Simulacao", endpoint.getNomeApi());
        assertEquals(10, endpoint.getQtdRequisicoes()); // 6 + 4 = 10
        assertEquals(100, endpoint.getTempoMedio()); // (6*80 + 4*130) / 10 = 100
        assertEquals(50, endpoint.getTempoMinimo());
        assertEquals(200, endpoint.getTempoMaximo()); // max(150, 200) = 200
        assertTrue(endpoint.getPercentualSucesso() > 0.0);
    }

    @Test
    void getTelemetriaHandlesNoTimer() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        io.micrometer.core.instrument.search.Search searchSimulacoes = mock(io.micrometer.core.instrument.search.Search.class);
        io.micrometer.core.instrument.search.Search searchPorDia = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.tag("uri", "/simulacoes")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.timer()).thenReturn(null);
        when(searchSimulacoes.meters()).thenReturn(Collections.emptyList());

        when(searchSimulacoes.tag("uri", "/simulacoes/por-dia")).thenReturn(searchPorDia);
        when(searchPorDia.timer()).thenReturn(null);
        when(searchPorDia.meters()).thenReturn(Collections.emptyList());

        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        when(simulacaoCalculoService.getMinElapsedTime()).thenReturn(50L);
        SimulacaoTelemetriaService service = new SimulacaoTelemetriaService(simulacaoCalculoService);

        SimulacaoNaoEncontradaException exception = assertThrows(
            SimulacaoNaoEncontradaException.class,
            () -> service.getTelemetria(meterRegistry)
        );
        assertTrue(exception.getMessage().contains("Nenhuma simulação encontrada. Crie uma simulação para gerar dados de telemetria."));
    }

    @Test
    void getTelemetriaWithOnlySimulacoesEndpoint() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        Timer timerSimulacoes = mock(Timer.class);
        io.micrometer.core.instrument.search.Search searchSimulacoes = mock(io.micrometer.core.instrument.search.Search.class);
        io.micrometer.core.instrument.search.Search searchPorDia = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.tag("uri", "/simulacoes")).thenReturn(searchSimulacoes);
        when(searchSimulacoes.timer()).thenReturn(timerSimulacoes);
        when(searchSimulacoes.meters()).thenReturn(Collections.singletonList(timerSimulacoes));

        when(searchSimulacoes.tag("uri", "/simulacoes/por-dia")).thenReturn(searchPorDia);
        when(searchPorDia.timer()).thenReturn(null);
        when(searchPorDia.meters()).thenReturn(Collections.emptyList());

        when(timerSimulacoes.count()).thenReturn(5L);
        when(timerSimulacoes.mean(TimeUnit.MILLISECONDS)).thenReturn(90.0);
        when(timerSimulacoes.max(TimeUnit.MILLISECONDS)).thenReturn(180.0);

        io.micrometer.core.instrument.Meter.Id meterId = mock(io.micrometer.core.instrument.Meter.Id.class);
        when(timerSimulacoes.getId()).thenReturn(meterId);
        when(meterId.getTag("status")).thenReturn("200");

        SimulacaoCalculoService simulacaoCalculoService = mock(SimulacaoCalculoService.class);
        when(simulacaoCalculoService.getMinElapsedTime()).thenReturn(40L);
        SimulacaoTelemetriaService service = new SimulacaoTelemetriaService(simulacaoCalculoService);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals("Simulacao", endpoint.getNomeApi());
        assertEquals(5, endpoint.getQtdRequisicoes());
        assertEquals(90, endpoint.getTempoMedio());
        assertEquals(40, endpoint.getTempoMinimo());
        assertEquals(180, endpoint.getTempoMaximo());
        assertTrue(endpoint.getPercentualSucesso() > 0.0);
    }
}