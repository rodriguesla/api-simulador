package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.service.SimulacaoListaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoPorDiaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoTelemetriaService;

import io.micrometer.core.instrument.MeterRegistry;

import com.github.rodriguesla.api_simulador.dto.SimulacaoListaResponseDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacoesPorDiaResponseDTO;
import com.github.rodriguesla.api_simulador.dto.TelemetriaResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SimulacaoViewController {
    private static final String VIEW_SIMULACOES = "simulacoes";
    private static final String VIEW_SIMULACOES_POR_DIA = "simulacoes_por_dia";
    private static final String VIEW_TELEMETRIA = "telemetria";
    private static final String ATTR_SIMULACOES = "simulacoes";
    private static final String ATTR_DATA_REFERENCIA = "dataReferencia";
    private static final String ATTR_SIMULACOES_POR_DIA = "simulacoesPorDia";
    private static final String ATTR_TELEMETRIA = "telemetria";

    private final SimulacaoListaService simulacaoListaService;
    private final SimulacaoPorDiaService simulacaoPorDiaService;
    private final SimulacaoTelemetriaService simulacaoTelemetriaService;
    private final MeterRegistry meterRegistry;

    @GetMapping("/simulacoes-view")
    public String listarSimulacoes(Model model) {
        SimulacaoListaResponseDTO simulacoes = simulacaoListaService.buscarSimulacoesLista(1, 200);
        model.addAttribute(ATTR_SIMULACOES, simulacoes.getRegistros());
        return VIEW_SIMULACOES;
    }

    @GetMapping("/simulacoes-por-dia-view")
    public String listarSimulacoesPorDia(@RequestParam(required = false) String data, Model model) {
        if (data == null || data.isBlank()) {
            data = java.time.LocalDate.now().toString();
        }
        SimulacoesPorDiaResponseDTO simulacoesPorDia = simulacaoPorDiaService.buscarSimulacoesPorDia(data);
        model.addAttribute(ATTR_DATA_REFERENCIA, simulacoesPorDia.getDataReferencia());
        model.addAttribute(ATTR_SIMULACOES_POR_DIA, simulacoesPorDia.getSimulacoes());
        return VIEW_SIMULACOES_POR_DIA;
    }

    @GetMapping("/telemetria-view")
    public String mostrarTelemetria(Model model) {
        TelemetriaResponseDTO telemetria = simulacaoTelemetriaService.getTelemetria(meterRegistry);
        model.addAttribute(ATTR_TELEMETRIA, telemetria);
        return VIEW_TELEMETRIA;
    }
}