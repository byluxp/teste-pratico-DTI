package com.example.demo.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gerencia os clientes SSE conectados e faz o broadcast thread-safe do estado
 * da simulação (drones, pedidos e voos) sempre que ela é atualizada.
 */
@Service
public class SseBroadcastService {

    private static final long EMITTER_TIMEOUT_MS = 30L * 60 * 1000; // 30 minutos

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(Map<String, Object> snapshotInicial) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        // Envia o estado atual imediatamente, sem esperar o próximo tick da simulação
        enviarEvento(emitter, snapshotInicial);
        return emitter;
    }

    public void broadcast(Map<String, Object> snapshot) {
        for (SseEmitter emitter : emitters) {
            enviarEvento(emitter, snapshot);
        }
    }

    private void enviarEvento(SseEmitter emitter, Map<String, Object> payload) {
        try {
            emitter.send(SseEmitter.event().name("simulacao-update").data(payload, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }
    }
}
