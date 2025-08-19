//package com.github.rodriguesla.api_simulador;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
//import com.azure.messaging.eventhubs.EventHubClientBuilder;
//import com.azure.messaging.eventhubs.EventHubProducerClient;
//import com.azure.messaging.eventhubs.EventData;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
////@Service
//public class EventHubProducerService {
//    private final EventHubProducerClient producerClient;
//    private final ObjectMapper objectMapper;
//
//    public EventHubProducerService(
//            @Value("${azure.eventhub.connection-string}") String connectionString,
//            @Value("${azure.eventhub.entity-path}") String entityPath) {
//        this.producerClient = new EventHubClientBuilder()
//                .connectionString(connectionString, entityPath)
//                .buildProducerClient();
//        this.objectMapper = new ObjectMapper();
//    }
//
//    public void sendSimulation(SimulacaoResponseDTO simulacao) {
//        // Bypass Event Hub sending for now
//        return;
//        // Original code below (kept for reference)
//    /*
//    try {
//        String json = objectMapper.writeValueAsString(simulacao);
//        EventData eventData = new EventData(json);
//        producerClient.send(java.util.Collections.singletonList(eventData));
//    } catch (Exception e) {
//        System.err.println("Failed to send event to EventHub: " + e.getMessage());
//    }
//    */
//    }
//
//    public void close() {
//        producerClient.close();
//    }
//}
//
