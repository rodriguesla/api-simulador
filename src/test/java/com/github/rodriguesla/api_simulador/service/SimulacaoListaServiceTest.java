package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.SimulacaoListaResponseDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRegistroDTO;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulacaoListaServiceTest {

    @Test
    void buscarSimulacoesListaReturnsCorrectResponse() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);

        Simulacao simulacao1 = Simulacao.builder().id(1L).valorDesejado(1000.0).build();
        Simulacao simulacao2 = Simulacao.builder().id(2L).valorDesejado(2000.0).build();

        when(simulacaoRepository.findAll()).thenReturn(List.of(simulacao1, simulacao2));
        when(simulacaoRepository.count()).thenReturn(2L);

        SimulacaoListaService service = new SimulacaoListaService(simulacaoRepository);

        SimulacaoListaResponseDTO response = service.buscarSimulacoesLista(1, 2);

        assertNotNull(response);
        assertEquals(1, response.getPagina());
        assertEquals(2, response.getQtdRegistros());
        assertEquals(2, response.getQtdRegistrosPagina());
        assertEquals(2, response.getRegistros().size());
        assertTrue(response.getRegistros().get(0) instanceof SimulacaoRegistroDTO);
    }

    @Test
    void buscarSimulacoesListaHandlesEmptyList() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);

        when(simulacaoRepository.findAll()).thenReturn(List.of());
        when(simulacaoRepository.count()).thenReturn(0L);

        SimulacaoListaService service = new SimulacaoListaService(simulacaoRepository);

        SimulacaoListaResponseDTO response = service.buscarSimulacoesLista(1, 10);

        assertNotNull(response);
        assertEquals(0, response.getRegistros().size());
        assertEquals(0, response.getQtdRegistros());
    }

    @Test
    void buscarSimulacoesListaHandlesInvalidParams() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);

        Simulacao simulacao = Simulacao.builder()
                .id(1L)
                .valorDesejado(1000.0)
                .build();
        when(simulacaoRepository.findAll()).thenReturn(List.of(simulacao));
        when(simulacaoRepository.count()).thenReturn(1L);

        SimulacaoListaService service = new SimulacaoListaService(simulacaoRepository);

        SimulacaoListaResponseDTO response = service.buscarSimulacoesLista(0, 0);

        assertNotNull(response);
        assertEquals(1, response.getRegistros().size());
        assertEquals(1, response.getQtdRegistros());
    }
}