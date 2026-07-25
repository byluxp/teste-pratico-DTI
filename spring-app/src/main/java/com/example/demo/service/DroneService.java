package com.example.demo.service;

import com.example.demo.model.Drone;
import com.example.demo.model.StatusDrone;
import com.example.demo.repository.DroneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DroneService {
    
    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    
    public Drone criarDrone(Drone drone) {
        double capacidadeMaxima = ThreadLocalRandom.current().nextDouble(1.0, 2.5 + 1e-9);
        double autonomiaMaxima = ThreadLocalRandom.current().nextDouble(8.0, 16.0 + 1e-9);

        long totalDrones = droneRepository.count();
        String codigo = String.format("DD%02d", totalDrones + 1);

        drone.setCapacidadeMaximaKg(capacidadeMaxima);
        drone.setAutonomiaMaximaKm(autonomiaMaxima);
        drone.setAutonomiaAtualKm(autonomiaMaxima);
        drone.setStatus(StatusDrone.IDLE);
        drone.setCodigo(codigo);
        return droneRepository.save(drone);
    }
    
    public List<Drone> listarDrones() {
        return droneRepository.findAll();
    }
}
