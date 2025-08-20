package com.github.rodriguesla.api_simulador.mapper;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.produto.model.Produto;
import com.github.rodriguesla.api_simulador.simulacao.model.Simulacao;
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

    public static SimulacoesPorDiaResponseDTO toSimulacoesPorDiaResponseDTO(
            String dataReferencia,
            List<Simulacao> simulacoes,
            Map<Integer, Produto> produtoMap) {

        logger.info("Mapeando {} simulações para a data {}", simulacoes.size(), dataReferencia);

        Map<Integer, List<Simulacao>> byProduto = simulacoes.stream()
                .collect(Collectors.groupingBy(Simulacao::getProdutoId));
        logger.debug("Simulações agrupadas por produto: {}", byProduto.keySet());

        List<SimulacaoPorDiaProdutoDTO> simulacoesPorProduto = byProduto.entrySet().stream()
                .map(entry -> {
                    Integer produtoId = entry.getKey();
                    List<Simulacao> list = entry.getValue();
                    logger.debug("Mapeando produtoId {} com {} simulações", produtoId, list.size());

                    Produto produto = produtoMap.get(produtoId);
                    if (produto == null) {
                        logger.warn("Produto não encontrado para produtoId {}", produtoId);
                    } else {
                        logger.debug("Produto encontrado: {} (taxaJuros: {})", produto.getNome(), produto.getTaxaJuros());
                    }

                    BigDecimal valorMedioPrestacao = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(s -> (s.getResultadoSac() + s.getResultadoPrice()) / 2)
                                    .average()
                                    .orElse(0)
                    );
                    BigDecimal valorTotalDesejado = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(Simulacao::getValorDesejado)
                                    .sum()
                    );
                    BigDecimal valorTotalCredito = BigDecimal.valueOf(
                            list.stream()
                                    .mapToDouble(s -> s.getResultadoSac() + s.getResultadoPrice())
                                    .sum()
                    );

                    logger.info("ProdutoId {}: valorMedioPrestacao={}, valorTotalDesejado={}, valorTotalCredito={}",
                            produtoId, valorMedioPrestacao, valorTotalDesejado, valorTotalCredito);

                    return SimulacaoPorDiaProdutoDTO.builder()
                            .codigoProduto(produtoId != null ? produtoId.longValue() : null)
                            .descricaoProduto(produto != null ? produto.getNome() : null)
                            .taxaMediaJuro(produto != null ? produto.getTaxaJuros() : null)
                            .valorMedioPrestacao(valorMedioPrestacao)
                            .valorTotalDesejado(valorTotalDesejado)
                            .valorTotalCredito(valorTotalCredito)
                            .build();
                })
                .collect(Collectors.toList());

        logger.info("Mapeamento finalizado. Retornando DTO com {} produtos", simulacoesPorProduto.size());

        return SimulacoesPorDiaResponseDTO.builder()
                .dataReferencia(dataReferencia)
                .simulacoes(simulacoesPorProduto)
                .build();
    }
}