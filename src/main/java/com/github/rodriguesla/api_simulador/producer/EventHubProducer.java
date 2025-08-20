package com.github.rodriguesla.api_simulador.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventHubProducer {
    private static final Logger logger = LoggerFactory.getLogger(EventHubProducer.class);

    private final EventHubProducerClient producerClient;
    private final ObjectMapper objectMapper;

    public EventHubProducer(
            @Value("${azure.eventhub.connection-string}") String connectionString,
            @Value("${azure.eventhub.entity-path}") String entityPath) {
        this.producerClient = new EventHubClientBuilder()
                .connectionString(connectionString, entityPath)
                .buildProducerClient();
        this.objectMapper = new ObjectMapper();
    }

    public void sendSimulation(SimulacaoResponseDTO simulacao) {
        try {
            String json = objectMapper.writeValueAsString(simulacao);
            logger.info("Enviando evento de simulação para o EventHub: {}", json);
            EventData eventData = new EventData(json);
            producerClient.send(java.util.Collections.singletonList(eventData));
            logger.info("Evento de simulação enviado com sucesso.");
        } catch (Exception e) {
            logger.error("Falha ao enviar evento para o EventHub: {}", e.getMessage(), e);
        }
    }

    public void close() {
        producerClient.close();
        logger.info("Cliente do EventHub fechado.");
    }
}

