package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.SimulacoesPorDiaResponseDTO;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.produto.repository.ProdutoRepository;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.exception.RequisicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulacaoPorDiaServiceTest {

    @Test
    void buscarSimulacoesPorDiaReturnsCorrectResponse() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);

        Simulacao simulacao = Simulacao.builder()
                .id(1L)
                .produtoId(1)
                .dataSimulacao(LocalDateTime.of(2024, 6, 1, 10, 0))
                .resultadoSac(123.45)
                .resultadoPrice(456.78)
                .build();
        Produto produto = Produto.builder()
                .id(1)
                .nome("Produto Teste")
                .build();

        when(simulacaoRepository.findByDataSimulacaoBetween(any(), any()))
                .thenReturn(List.of(simulacao));
        when(produtoRepository.findAll()).thenReturn(List.of(produto));

        SimulacaoPorDiaService service = new SimulacaoPorDiaService(simulacaoRepository, produtoRepository);

        SimulacoesPorDiaResponseDTO response = service.buscarSimulacoesPorDia("2024-06-01");

        assertNotNull(response);
        assertEquals("2024-06-01", response.getDataReferencia());
        assertFalse(response.getSimulacoes().isEmpty());
        assertEquals("Produto Teste", response.getSimulacoes().getFirst().getDescricaoProduto());
    }

    @Test
    void buscarSimulacoesPorDiaBlankDateThrowsException() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);

        SimulacaoPorDiaService service = new SimulacaoPorDiaService(simulacaoRepository, produtoRepository);

        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarSimulacoesPorDia(""));
    }

    @Test
    void buscarSimulacoesPorDiaInvalidDateThrowsException() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);

        SimulacaoPorDiaService service = new SimulacaoPorDiaService(simulacaoRepository, produtoRepository);

        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarSimulacoesPorDia("invalid-date"));
    }

    @Test
    void buscarTodosProdutosReturnsAllProducts() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);

        Produto produto1 = Produto.builder().id(1).nome("Produto 1").build();
        Produto produto2 = Produto.builder().id(2).nome("Produto 2").build();

        when(produtoRepository.findAll()).thenReturn(List.of(produto1, produto2));

        SimulacaoPorDiaService service = new SimulacaoPorDiaService(simulacaoRepository, produtoRepository);

        List<Produto> produtos = service.buscarTodosProdutos();

        assertEquals(2, produtos.size());
        assertEquals(produto1, produtos.get(0));
        assertEquals(produto2, produtos.get(1));
        verify(produtoRepository, times(1)).findAll();
    }
}