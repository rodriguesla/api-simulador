package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.entities.auditoria.model.AuditLog;
import com.github.rodriguesla.api_simulador.entities.auditoria.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SimulacaoAuditLogService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String username, String endpoint, String requestData, String responseStatus, String ipAddress, String errorMessage) {
        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUsername(username);
        log.setEndpoint(endpoint);
        log.setRequestData(requestData);
        log.setResponseStatus(responseStatus);
        log.setIpAddress(ipAddress);
        log.setErrorMessage(errorMessage);
        auditLogRepository.save(log);
    }
}
