package com.example.demo.controller;

import com.example.demo.model.Drone;
import com.example.demo.service.DroneService;
import com.example.demo.dto.AtualizarStatusDroneDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drones")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Drone criarDrone(@RequestBody Drone drone) {
        return droneService.criarDrone(drone);
    }

    @GetMapping("/status")
    public List<Drone> listarDrones() {
        return droneService.listarDrones();
    }

    // Endpoint extra para simular mudança de status/bateria
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable Long id, @RequestBody AtualizarStatusDroneDTO dto) {
        // Lógica de atualização de estado orientada a eventos[cite: 1]
        return ResponseEntity.noContent().build();
    }

}
