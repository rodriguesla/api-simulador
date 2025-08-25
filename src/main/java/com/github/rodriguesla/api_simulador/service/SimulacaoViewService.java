package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulacaoViewService {
    private static final String ATTR_SIMULACOES = "simulacoes";
    private static final String ATTR_DATA_REFERENCIA = "dataReferencia";
    private static final String ATTR_SIMULACOES_POR_DIA = "simulacoesPorDia";
    private static final String ATTR_TELEMETRIA = "telemetria";

    private final SimulacaoListaService simulacaoListaService;
    private final SimulacaoPorDiaService simulacaoPorDiaService;
    private final SimulacaoTelemetriaService simulacaoTelemetriaService;
    private final MeterRegistry meterRegistry;

    public SimulacaoViewService(SimulacaoListaService simulacaoListaService,
                                SimulacaoPorDiaService simulacaoPorDiaService,
                                SimulacaoTelemetriaService simulacaoTelemetriaService,
                                MeterRegistry meterRegistry) {
        this.simulacaoListaService = simulacaoListaService;
        this.simulacaoPorDiaService = simulacaoPorDiaService;
        this.simulacaoTelemetriaService = simulacaoTelemetriaService;
        this.meterRegistry = meterRegistry;
    }

    private String formatBigDecimal(java.math.BigDecimal value) {
        if (value == null) return "";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);
        return nf.format(value);
    }

    public String listarSimulacoes(Model model) {
        try {
            SimulacaoListaResponseDTO simulacoes = simulacaoListaService.buscarSimulacoesLista(0, 200);
            if (simulacoes.getRegistros() == null || simulacoes.getRegistros().isEmpty()) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhuma simulação foi encontrada no sistema.");
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            var formattedList = simulacoes.getRegistros().stream().map(simulacao -> Map.of(
                "idSimulacao", simulacao.getIdSimulacao(),
                "prazo", simulacao.getPrazo(),
                "valorDesejado", formatBigDecimal(simulacao.getValorDesejado()),
                "valorTotalParcelas", formatBigDecimal(simulacao.getValorTotalParcelas()),
                "valorSacPriceSac", formatBigDecimal(simulacao.getValorSacPrice() != null ? simulacao.getValorSacPrice().getSac() : null),
                "valorSacPricePrice", formatBigDecimal(simulacao.getValorSacPrice() != null ? simulacao.getValorSacPrice().getPrice() : null)
            )).collect(Collectors.toList());
            model.addAttribute(ATTR_SIMULACOES, formattedList);
            return "simulacoes";
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Nenhuma simulação encontrada")) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhuma simulação foi encontrada no sistema.");
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            model.addAttribute("erro", "Erro Interno");
            model.addAttribute("mensagem", "Ocorreu um erro ao buscar as simulações: " + e.getMessage());
            model.addAttribute("codigo", 500);
            model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
            return "erro";
        }
    }

    public String listarSimulacoesPorDia(String data, Model model) {
        try {
            if (data == null || data.isBlank()) {
                data = java.time.LocalDate.now().toString();
            }
            SimulacoesPorDiaResponseDTO simulacoesPorDia = simulacaoPorDiaService.buscarSimulacoesPorDia(data);
            if (simulacoesPorDia.getSimulacoes() == null || simulacoesPorDia.getSimulacoes().isEmpty()) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhuma simulação foi encontrada para a data: " + data);
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            var formattedList = simulacoesPorDia.getSimulacoes().stream().map(simulacao -> Map.of(
                "codigoProduto", simulacao.getCodigoProduto(),
                "descricaoProduto", simulacao.getDescricaoProduto(),
                "taxaMediaJuro", formatBigDecimal(simulacao.getTaxaMediaJuro()),
                "valorMedioPrestacao", formatBigDecimal(simulacao.getValorMedioPrestacao()),
                "valorTotalDesejado", formatBigDecimal(simulacao.getValorTotalDesejado()),
                "valorTotalCredito", formatBigDecimal(simulacao.getValorTotalCredito())
            )).collect(Collectors.toList());
            model.addAttribute(ATTR_DATA_REFERENCIA, simulacoesPorDia.getDataReferencia());
            model.addAttribute(ATTR_SIMULACOES_POR_DIA, formattedList);
            return "simulacoes_por_dia";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("Nenhuma simulação encontrada") ||
                                          e.getMessage().contains("não encontrada"))) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhuma simulação foi encontrada para a data: " + data);
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            model.addAttribute("erro", "Erro Interno");
            model.addAttribute("mensagem", "Ocorreu um erro ao buscar simulações por dia: " + e.getMessage());
            model.addAttribute("codigo", 500);
            model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
            return "erro";
        }
    }

    public String mostrarTelemetria(Model model) {
        try {
            TelemetriaResponseDTO telemetria = simulacaoTelemetriaService.getTelemetria(meterRegistry);
            if (telemetria.getListaEndpoints() == null || telemetria.getListaEndpoints().isEmpty()) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhum dado de telemetria foi encontrado.");
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            model.addAttribute(ATTR_TELEMETRIA, telemetria);
            return "telemetria";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("Nenhuma simulação encontrada") ||
                                          e.getMessage().contains("não encontrada") ||
                                          e.getMessage().contains("não encontrado") ||
                                          e.getMessage().contains("Crie uma simulação para gerar dados"))) {
                model.addAttribute("erro", "Nenhum Dado Encontrado");
                model.addAttribute("mensagem", "Nenhuma simulação foi encontrada. Crie uma simulação para gerar dados de telemetria.");
                model.addAttribute("codigo", 404);
                model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
                return "erro";
            }
            model.addAttribute("erro", "Erro Interno");
            model.addAttribute("mensagem", "Ocorreu um erro ao buscar a telemetria: " + e.getMessage());
            model.addAttribute("codigo", 500);
            model.addAttribute("timestamp", java.time.LocalDateTime.now().toString());
            return "erro";
        }
    }
}

