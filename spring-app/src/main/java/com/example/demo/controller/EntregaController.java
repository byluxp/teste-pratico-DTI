package com.example.demo.controller;

import com.example.demo.model.Entrega;
import com.example.demo.repository.EntregaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    // Histórico agrupado por viagem de entrega (Entrega), com todos os pedidos daquele voo concluído
    @GetMapping("/historico")
    public List<Entrega> listarHistorico() {
        return entregaRepository.findAllByOrderByDataHoraDesc();
    }
}
