package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.entities.auditoria.model.AuditLog;
import com.github.rodriguesla.api_simulador.entities.auditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogViewControllerTest {
    private AuditLogRepository auditLogRepository;
    private AuditLogViewController controller;
    private Model model;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        controller = new AuditLogViewController();
        try {
            var field = AuditLogViewController.class.getDeclaredField("auditLogRepository");
            field.setAccessible(true);
            field.set(controller, auditLogRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        model = new ExtendedModelMap();
    }

    @Test
    void testViewAuditwithLogsreturnsAuditoria() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.findAll(any(Sort.class))).thenReturn(List.of(log));

        String view = controller.viewAudit(model);
        assertEquals("auditoria", view);
        assertTrue(model.containsAttribute("logs"));
        List<?> logs = (List<?>) model.getAttribute("logs");
        assertEquals(1, logs.size());
    }

    @Test
    void testViewAuditnoLogsreturnsErro() {
        when(auditLogRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        String view = controller.viewAudit(model);
        assertEquals("erro", view);
        assertEquals("Sem registros de auditoria", model.getAttribute("erro"));
        assertEquals("Nenhum registro de auditoria foi encontrado no sistema.", model.getAttribute("mensagem"));
        assertEquals("404", model.getAttribute("codigo"));
        assertNotNull(model.getAttribute("timestamp"));
    }
}

