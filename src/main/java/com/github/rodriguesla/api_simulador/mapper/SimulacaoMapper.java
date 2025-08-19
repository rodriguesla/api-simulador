package com.github.rodriguesla.api_simulador.mapper;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.model.Simulacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulacaoMapper {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoMapper.class);

    public static SimulacaoRegistroDTO toRegistroDTO(Simulacao s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String idSimulacaoStr = s.getDataSimulacao() != null ? s.getDataSimulacao().format(formatter) : null;

        return SimulacaoRegistroDTO.builder()
                .idSimulacao(idSimulacaoStr)
                .valorDesejado(BigDecimal.valueOf(s.getValorDesejado()))
                .prazo(s.getPrazo())
                .valorTotalParcelas(
                        ValorTotalParcelasDTO.builder()
                                .sac(s.getResultadoSac() != null ? BigDecimal.valueOf(s.getResultadoSac()) : null)
                                .price(s.getResultadoPrice() != null ? BigDecimal.valueOf(s.getResultadoPrice()) : null)
                                .build()
                )
                .build();
    }

    public static SimulacoesPorDiaResponseDTO toSimulacoesPorDiaResponseDTO(String dataReferencia, List<Simulacao> simulacoes) {
        logger.info("Mapeando {} simulacoes para a data {}", simulacoes.size(), dataReferencia);

        if (simulacoes.isEmpty()) {
            logger.warn("Simulacoes nao encontradas para a data {}", dataReferencia);
        }
        // Agrupar por ID
        Map<Long, List<Simulacao>> byProduto = simulacoes.stream()
                .collect(Collectors.groupingBy(s -> s.getProduto().getId().longValue()));

        List<SimulacaoPorDiaProdutoDTO> simulacoesPorProduto = byProduto.values().stream()
                .map(list -> {
                    Simulacao first = list.get(0);
                    // Media da taxa de juros
                    BigDecimal taxaMedia = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(s -> s.getProduto().getTaxaJuros().doubleValue())
                                    .average()
                                    .orElse(0)
                    );
                    // Media do valor das parcelas (para o SAC e PRICE)
                    BigDecimal valorMedioPrestacao = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(s -> (s.getResultadoSac() + s.getResultadoPrice()) / 2)
                                    .average()
                                    .orElse(0)
                    );
                    // Valor total
                    BigDecimal valorTotalDesejado = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(Simulacao::getValorDesejado)
                                    .sum()
                    );
                    // Total do valor de credito (soma de SAC e PRICE)
                    BigDecimal valorTotalCredito = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(s -> s.getResultadoSac() + s.getResultadoPrice())
                                    .sum()
                    );
                    return SimulacaoPorDiaProdutoDTO.builder()
                            .codigoProduto(first.getProduto().getId().longValue())
                            .descricaoProduto(first.getProduto().getNome())
                            .taxaMediaJuro(taxaMedia)
                            .valorMedioPrestacao(valorMedioPrestacao)
                            .valorTotalDesejado(valorTotalDesejado)
                            .valorTotalCredito(valorTotalCredito)
                            .build();
                })
                .collect(Collectors.toList());

        return SimulacoesPorDiaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(simulacoesPorProduto)
                .build();
    }
}