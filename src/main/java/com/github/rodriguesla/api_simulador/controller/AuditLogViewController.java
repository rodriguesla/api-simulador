package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.entities.auditoria.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class AuditLogViewController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/auditoria-view")
    public String viewAudit(Model model) {
        var logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        if (logs == null || logs.isEmpty()) {
            model.addAttribute("erro", "Sem registros de auditoria");
            model.addAttribute("mensagem", "Nenhum registro de auditoria foi encontrado no sistema.");
            model.addAttribute("codigo", "404");
            model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            return "erro";
        }
        model.addAttribute("logs", logs);
        return "auditoria";
    }
}
