package com.example.demo.controller;

import com.example.demo.config.DataSeeder;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.ObstaculoRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reset")
public class ResetController {

    private final VooRepository vooRepository;
    private final PedidoRepository pedidoRepository;
    private final ObstaculoRepository obstaculoRepository;
    private final DroneRepository droneRepository;
    private final DataSeeder dataSeeder;

    public ResetController(VooRepository vooRepository, PedidoRepository pedidoRepository, ObstaculoRepository obstaculoRepository, DroneRepository droneRepository, DataSeeder dataSeeder) {
        this.vooRepository = vooRepository;
        this.pedidoRepository = pedidoRepository;
        this.obstaculoRepository = obstaculoRepository;
        this.droneRepository = droneRepository;
        this.dataSeeder = dataSeeder;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> resetDatabase() {
        // Pedidos têm FK para Voo, então deletar voo pode precisar de cascading ou desligamento.
        // Já que Voo tem CascadeType.ALL, deletar voos deletará pedidos amarrados (se configurado corretamente).
        // Mas os pedidos pendentes não tem voo. Deletamos todos.
        pedidoRepository.deleteAll();
        vooRepository.deleteAll();
        obstaculoRepository.deleteAll();
        droneRepository.deleteAll();

        // Recria drone
        dataSeeder.seedDrone();

        return ResponseEntity.ok().build();
    }
}
