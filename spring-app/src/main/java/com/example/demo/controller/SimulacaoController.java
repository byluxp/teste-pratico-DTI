package com.example.demo.controller;

import com.example.demo.service.SimulacaoService;
import com.example.demo.service.SseBroadcastService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/simulacao")
public class SimulacaoController {

    private final SseBroadcastService sseBroadcastService;
    private final SimulacaoService simulacaoService;

    public SimulacaoController(SseBroadcastService sseBroadcastService, SimulacaoService simulacaoService) {
        this.sseBroadcastService = sseBroadcastService;
        this.simulacaoService = simulacaoService;
    }

    // Stream SSE com o estado em tempo real de drones, pedidos e voos
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseBroadcastService.subscribe(simulacaoService.obterSnapshotAtual());
    }
}
