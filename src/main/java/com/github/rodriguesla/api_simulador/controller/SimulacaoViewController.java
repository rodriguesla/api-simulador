package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.service.SimulacaoViewService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SimulacaoViewController {
    private static final String VIEW_HOME = "home";


    private final SimulacaoViewService simulacaoViewService;

    @GetMapping("/simulacoes-view")
    public String listarSimulacoes(Model model) {
        return simulacaoViewService.listarSimulacoes(model);
    }

    @GetMapping("/simulacoes-por-dia-view")
    public String listarSimulacoesPorDia(@RequestParam(required = false) String data, Model model) {
        return simulacaoViewService.listarSimulacoesPorDia(data, model);
    }

    @GetMapping("/telemetria-view")
    public String mostrarTelemetria(Model model) {
        return simulacaoViewService.mostrarTelemetria(model);
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return VIEW_HOME;
    }
}