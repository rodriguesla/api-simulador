package com.github.rodriguesla.api_simulador.mapper;

import com.github.rodriguesla.api_simulador.dto.SimulacaoPorDiaProdutoDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRegistroDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacoesPorDiaResponseDTO;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulacaoMapperTest {

    @Test
    void testToRegistroDTOAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 10, 12, 0);
        Simulacao simulacao = Simulacao.builder()
                .id(1L)
                .produtoId(100)
                .valorDesejado(5000.0)
                .prazo(24)
                .resultadoSac(5200.0)
                .resultadoPrice(5300.0)
                .dataSimulacao(now)
                .envelopeJson("{}")
                .build();

        SimulacaoRegistroDTO dto = SimulacaoMapper.toRegistroDTO(simulacao);

        assertEquals("20240610", dto.getIdSimulacao());
        assertEquals(BigDecimal.valueOf(5000.0), dto.getValorDesejado());
        assertEquals(24, dto.getPrazo());
        assertEquals(BigDecimal.valueOf((5200.0 + 5300.0) / 2).setScale(2), dto.getValorTotalParcelas());
        assertNotNull(dto.getValorSacPrice());
        assertEquals(BigDecimal.valueOf(5200.0), dto.getValorSacPrice().getSac());
        assertEquals(BigDecimal.valueOf(5300.0), dto.getValorSacPrice().getPrice());
    }

    @Test
    void testToRegistroDTONullFields() {
        Simulacao simulacao = Simulacao.builder()
                .id(2L)
                .produtoId(101)
                .valorDesejado(10000.0)
                .prazo(36)
                .resultadoSac(null)
                .resultadoPrice(null)
                .dataSimulacao(null)
                .envelopeJson(null)
                .build();

        SimulacaoRegistroDTO dto = SimulacaoMapper.toRegistroDTO(simulacao);

        assertNull(dto.getIdSimulacao());
        assertEquals(BigDecimal.valueOf(10000.0), dto.getValorDesejado());
        assertEquals(36, dto.getPrazo());
        assertNull(dto.getValorTotalParcelas());
        assertNotNull(dto.getValorSacPrice());
        assertNull(dto.getValorSacPrice().getSac());
        assertNull(dto.getValorSacPrice().getPrice());
    }

    @Test
    void testToSimulacoesPorDiaResponseDTOMultipleProducts() {
        Simulacao s1 = Simulacao.builder()
                .id(1L)
                .produtoId(100)
                .valorDesejado(5000.0)
                .prazo(24)
                .resultadoSac(5200.0)
                .resultadoPrice(5300.0)
                .dataSimulacao(LocalDateTime.now())
                .envelopeJson("{}")
                .build();

        Simulacao s2 = Simulacao.builder()
                .id(2L)
                .produtoId(100)
                .valorDesejado(6000.0)
                .prazo(36)
                .resultadoSac(6200.0)
                .resultadoPrice(6300.0)
                .dataSimulacao(LocalDateTime.now())
                .envelopeJson("{}")
                .build();

        Simulacao s3 = Simulacao.builder()
                .id(3L)
                .produtoId(101)
                .valorDesejado(7000.0)
                .prazo(48)
                .resultadoSac(7200.0)
                .resultadoPrice(7300.0)
                .dataSimulacao(LocalDateTime.now())
                .envelopeJson("{}")
                .build();

        Produto produto100 = Produto.builder()
                .id(100)
                .nome("Produto A")
                .taxaJuros(BigDecimal.valueOf(0.05))
                .prazoMinimo((short)12)
                .prazoMaximo((short)48)
                .valorMinimo(BigDecimal.valueOf(1000))
                .valorMaximo(BigDecimal.valueOf(10000))
                .build();

        Map<Integer, Produto> produtoMap = new HashMap<>();
        produtoMap.put(100, produto100); // Produto 101 intentionally missing

        List<Simulacao> simulacoes = Arrays.asList(s1, s2, s3);

        SimulacoesPorDiaResponseDTO response = SimulacaoMapper.toSimulacoesPorDiaResponseDTO(
                "20240610", simulacoes, produtoMap);

        assertEquals("20240610", response.getDataReferencia());
        assertEquals(2, response.getSimulacoes().size());

        SimulacaoPorDiaProdutoDTO dtoA = response.getSimulacoes().stream()
                .filter(dto -> dto.getCodigoProduto().equals(100L))
                .findFirst().orElse(null);

        assertNotNull(dtoA);
        assertEquals("Produto A", dtoA.getDescricaoProduto());
        assertEquals(BigDecimal.valueOf(0.05), dtoA.getTaxaMediaJuro());
        assertEquals(BigDecimal.valueOf(
                ((5200.0 + 5300.0) / 2 + (6200.0 + 6300.0) / 2) / 2
        ), dtoA.getValorMedioPrestacao());
        assertEquals(BigDecimal.valueOf(5000.0 + 6000.0), dtoA.getValorTotalDesejado());
        assertEquals(BigDecimal.valueOf(5200.0 + 5300.0 + 6200.0 + 6300.0), dtoA.getValorTotalCredito());

        SimulacaoPorDiaProdutoDTO dtoB = response.getSimulacoes().stream()
                .filter(dto -> dto.getCodigoProduto().equals(101L))
                .findFirst().orElse(null);

        assertNotNull(dtoB);
        assertNull(dtoB.getDescricaoProduto());
        assertNull(dtoB.getTaxaMediaJuro());
        assertEquals(BigDecimal.valueOf((7200.0 + 7300.0) / 2), dtoB.getValorMedioPrestacao());
        assertEquals(BigDecimal.valueOf(7000.0), dtoB.getValorTotalDesejado());
        assertEquals(BigDecimal.valueOf(7200.0 + 7300.0), dtoB.getValorTotalCredito());
    }
}