package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.EndpointTelemetriaDTO;
import com.github.rodriguesla.api_simulador.dto.TelemetriaResponseDTO;
import com.github.rodriguesla.api_simulador.exception.SimulacaoNaoEncontradaException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SimulacaoTelemetriaService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoTelemetriaService.class);

    private final SimulacaoCalculoService simulacaoCalculoService;

    public SimulacaoTelemetriaService(SimulacaoCalculoService simulacaoCalculoService) {
        this.simulacaoCalculoService = simulacaoCalculoService;
    }

    public TelemetriaResponseDTO getTelemetria(MeterRegistry meterRegistry) {
        logger.info("Iniciando cálculo de telemetria para os endpoints /simulacoes e /simulacoes/por-dia.");
        String dataReferencia = LocalDate.now().toString();

        Timer timerSimulacoes = meterRegistry.find("http.server.requests")
                .tag("uri", "/simulacoes")
                .timer();

        Timer timerPorDia = meterRegistry.find("http.server.requests")
                .tag("uri", "/simulacoes/por-dia")
                .timer();

        long qtdRequisicoes = 0;
        long tempoMedio = 0;
        long tempoMinimo = simulacaoCalculoService.getMinElapsedTime() == Long.MAX_VALUE ? 0 : simulacaoCalculoService.getMinElapsedTime();
        long tempoMaximo = 0;
        double percentualSucesso = 0.0;

        long totalCount = 0;
        double totalMeanTime = 0;
        long maxTime = 0;

        if (timerSimulacoes != null) {
            logger.debug("Timer encontrado para /simulacoes.");
            long count = timerSimulacoes.count();
            totalCount += count;
            totalMeanTime += timerSimulacoes.mean(java.util.concurrent.TimeUnit.MILLISECONDS) * count;
            maxTime = Math.max(maxTime, (long) timerSimulacoes.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        }

        if (timerPorDia != null) {
            logger.debug("Timer encontrado para /simulacoes/por-dia.");
            long count = timerPorDia.count();
            totalCount += count;
            totalMeanTime += timerPorDia.mean(java.util.concurrent.TimeUnit.MILLISECONDS) * count;
            maxTime = Math.max(maxTime, (long) timerPorDia.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        }

        if (totalCount > 0) {
            tempoMedio = (long) (totalMeanTime / totalCount);
            tempoMaximo = maxTime;
            qtdRequisicoes = totalCount;
            logger.debug("Valores agregados calculados: qtdRequisicoes={}, tempoMedio={}, tempoMinimo={}, tempoMaximo={}", qtdRequisicoes, tempoMedio, tempoMinimo, tempoMaximo);
        } else {
            logger.warn("Nenhum timer encontrado para /simulacoes ou /simulacoes/por-dia.");
        }

        AtomicReference<Double> totalRequests = new AtomicReference<>(0.0);
        AtomicReference<Double> successRequests = new AtomicReference<>(0.0);

        meterRegistry.find("http.server.requests")
                .tag("uri", "/simulacoes")
                .meters()
                .forEach(meter -> {
                    if (meter instanceof Timer) {
                        totalRequests.updateAndGet(val -> val + ((Timer) meter).count());
                        String status = meter.getId().getTag("status");
                        if (status != null && status.startsWith("2")) {
                            successRequests.updateAndGet(val -> val + ((Timer) meter).count());
                        }
                    }
                });

        meterRegistry.find("http.server.requests")
                .tag("uri", "/simulacoes/por-dia")
                .meters()
                .forEach(meter -> {
                    if (meter instanceof Timer) {
                        totalRequests.updateAndGet(val -> val + ((Timer) meter).count());
                        String status = meter.getId().getTag("status");
                        if (status != null && status.startsWith("2")) {
                            successRequests.updateAndGet(val -> val + ((Timer) meter).count());
                        }
                    }
                });

        if (totalRequests.get() == 0) {
            logger.warn("Nenhuma requisição encontrada para os endpoints. Lançando SimulacaoNaoEncontradaException.");
            throw new SimulacaoNaoEncontradaException("Nenhuma simulação encontrada. Crie uma simulação para gerar dados de telemetria.");
        }

        if (totalRequests.get() > 0) {
            percentualSucesso = successRequests.get() / totalRequests.get();
            qtdRequisicoes = totalRequests.get().longValue();
            logger.debug("Taxa de sucesso agregada calculada: {}", percentualSucesso);
        }

        EndpointTelemetriaDTO endpoint = EndpointTelemetriaDTO.builder()
                .nomeApi("Simulacao")
                .qtdRequisicoes((int) qtdRequisicoes)
                .tempoMedio(tempoMedio)
                .tempoMinimo(tempoMinimo)
                .tempoMaximo(tempoMaximo)
                .percentualSucesso(percentualSucesso)
                .build();

        logger.info("DTO de telemetria agregado pronto: {}", endpoint);

        return TelemetriaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .listaEndpoints(Collections.singletonList(endpoint))
                .build();
    }
}