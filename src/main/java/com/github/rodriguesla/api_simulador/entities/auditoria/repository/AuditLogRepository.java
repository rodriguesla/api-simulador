package com.github.rodriguesla.api_simulador.entities.auditoria.repository;

import com.github.rodriguesla.api_simulador.entities.auditoria.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

