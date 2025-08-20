package com.github.rodriguesla.api_simulador.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

public class EventHubProducerTest {

    private EventHubProducerClient producerClient;
    private ObjectMapper objectMapper;
    private EventHubProducer producer;

    @BeforeEach
    void setUp() {
        producerClient = mock(EventHubProducerClient.class);
        objectMapper = mock(ObjectMapper.class);

        producer = mock(EventHubProducer.class, CALLS_REAL_METHODS);
        try {
            var pcField = EventHubProducer.class.getDeclaredField("producerClient");
            pcField.setAccessible(true);
            pcField.set(producer, producerClient);

            var omField = EventHubProducer.class.getDeclaredField("objectMapper");
            omField.setAccessible(true);
            omField.set(producer, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSendSimulationSuccess() throws Exception {
        SimulacaoResponseDTO dto = new SimulacaoResponseDTO();
        when(objectMapper.writeValueAsString(dto)).thenReturn("{\"key\":\"value\"}");

        producer.sendSimulation(dto);

        ArgumentCaptor<java.util.List<EventData>> captor = ArgumentCaptor.forClass(java.util.List.class);
        verify(producerClient).send(captor.capture());
        EventData eventData = captor.getValue().get(0);
        assert eventData.getBodyAsString().equals("{\"key\":\"value\"}");
    }

    @Test
    void testSendSimulationException() throws Exception {
        SimulacaoResponseDTO dto = new SimulacaoResponseDTO();
        when(objectMapper.writeValueAsString(dto)).thenThrow(new RuntimeException("Serialization error"));

        producer.sendSimulation(dto);

        verify(producerClient, never()).send(any(java.util.List.class));
    }

    @Test
    void testClose() {
        producer.close();
        verify(producerClient).close();
    }
}