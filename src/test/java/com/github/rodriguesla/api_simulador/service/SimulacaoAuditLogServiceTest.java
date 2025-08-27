package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.entities.auditoria.model.AuditLog;
import com.github.rodriguesla.api_simulador.entities.auditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulacaoAuditLogServiceTest {
    private AuditLogRepository auditLogRepository;
    private SimulacaoAuditLogService service;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        service = new SimulacaoAuditLogService();
        try {
            var field = SimulacaoAuditLogService.class.getDeclaredField("auditLogRepository");
            field.setAccessible(true);
            field.set(service, auditLogRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testLogSavesAuditLogWithCorrectFields() {
        String username = "user";
        String endpoint = "/api/test";
        String requestData = "data";
        String responseStatus = "200";
        String ipAddress = "127.0.0.1";
        String errorMessage = "error";

        service.log(username, endpoint, requestData, responseStatus, ipAddress, errorMessage);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals(username, log.getUsername());
        assertEquals(endpoint, log.getEndpoint());
        assertEquals(requestData, log.getRequestData());
        assertEquals(responseStatus, log.getResponseStatus());
        assertEquals(ipAddress, log.getIpAddress());
        assertEquals(errorMessage, log.getErrorMessage());
        assertNotNull(log.getTimestamp());
        assertTrue(log.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testLogHandlesNullErrorMessage() {
        service.log("user", "/api/test", "data", "200", "127.0.0.1", null);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertNull(log.getErrorMessage());
    }
}

