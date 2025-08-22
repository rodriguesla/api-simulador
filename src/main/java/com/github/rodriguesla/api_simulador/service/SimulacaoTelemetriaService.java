package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.EndpointTelemetriaDTO;
import com.github.rodriguesla.api_simulador.dto.TelemetriaResponseDTO;
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
        logger.info("Iniciando cálculo de telemetria para o endpoint /simulacoes.");
        String dataReferencia = LocalDate.now().toString();

        Timer timer = meterRegistry.find("http.server.requests")
                .tag("uri", "/simulacoes")
                .timer();

        long qtdRequisicoes = 0;
        long tempoMedio = 0;
        long tempoMinimo = simulacaoCalculoService.getMinElapsedTime() == Long.MAX_VALUE ? 0 : simulacaoCalculoService.getMinElapsedTime();
        long tempoMaximo = 0;
        double percentualSucesso = 0.0;

        if (timer != null) {
            logger.debug("Timer encontrado para /simulacoes.");
            qtdRequisicoes = timer.count();
            tempoMedio = (long) timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            tempoMaximo = (long) timer.max(java.util.concurrent.TimeUnit.MILLISECONDS);

            logger.debug("Valores calculados: qtdRequisicoes={}, tempoMedio={}, tempoMinimo={}, tempoMaximo={}", qtdRequisicoes, tempoMedio, tempoMinimo, tempoMaximo);
        } else {
            logger.warn("Nenhum timer encontrado para /simulacoes.");
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

        if (totalRequests.get() > 0) {
            percentualSucesso = successRequests.get() / totalRequests.get();
            qtdRequisicoes = totalRequests.get().longValue();
            logger.debug("Taxa de sucesso calculada: {}", percentualSucesso);
        }

        EndpointTelemetriaDTO endpoint = EndpointTelemetriaDTO.builder()
                .nomeApi("API Simulador")
                .qtdRequisicoes((int) qtdRequisicoes)
                .tempoMedio(tempoMedio)
                .tempoMinimo(tempoMinimo)
                .tempoMaximo(tempoMaximo)
                .percentualSucesso(percentualSucesso)
                .build();

        logger.info("DTO de telemetria pronto: {}", endpoint);

        return TelemetriaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .listaEndpoints(Collections.singletonList(endpoint))
                .build();
    }
}