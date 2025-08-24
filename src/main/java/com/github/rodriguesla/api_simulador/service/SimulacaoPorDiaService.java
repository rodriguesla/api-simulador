package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.SimulacoesPorDiaResponseDTO;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.produto.repository.ProdutoRepository;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.exception.RequisicaoInvalidaException;
import com.github.rodriguesla.api_simulador.mapper.SimulacaoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulacaoPorDiaService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoPorDiaService.class);
    private static final String MSG_SIMULACAO_NAO_ENCONTRADA = "Nenhuma simulação encontrada para o dia informado.";

    private final SimulacaoRepository simulacaoRepository;
    private final ProdutoRepository produtoRepository;

    public SimulacaoPorDiaService(SimulacaoRepository simulacaoRepository, ProdutoRepository produtoRepository) {
        this.simulacaoRepository = simulacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    public SimulacoesPorDiaResponseDTO buscarSimulacoesPorDia(String data) {
        if (data == null || data.isBlank()) {
            throw new RequisicaoInvalidaException("O parâmetro 'data' é obrigatório e não pode estar em branco.");
        }
        LocalDate dia;
        try {
            dia = LocalDate.parse(data);
        } catch (Exception e) {
            throw new RequisicaoInvalidaException("O parâmetro 'data' deve ser uma data válida no formato yyyy-MM-dd.");
        }
        LocalDateTime start = dia.atStartOfDay();
        LocalDateTime end = dia.atTime(LocalTime.MAX);
        List<Simulacao> simulacoes = simulacaoRepository.findByDataSimulacaoBetween(start, end);
        if (simulacoes.isEmpty()) {
            throw new com.github.rodriguesla.api_simulador.exception.SimulacaoNaoEncontradaException(MSG_SIMULACAO_NAO_ENCONTRADA);
        }
        Map<Integer, Produto> produtoMap = buscarProdutoMap();
        return SimulacaoMapper.toSimulacoesPorDiaResponseDTO(data, simulacoes, produtoMap);
    }

    public List<Produto> buscarTodosProdutos() {
        List<Produto> produtos = produtoRepository.findAll();
        logger.info("Total de produtos encontrados: {}", produtos.size());
        return produtos;
    }

    public Map<Integer, Produto> buscarProdutoMap() {
        List<Produto> produtos = buscarTodosProdutos();
        return produtos.stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));
    }
}