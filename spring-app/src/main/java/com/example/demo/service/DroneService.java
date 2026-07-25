package com.example.demo.service;

import com.example.demo.model.Drone;
import com.example.demo.model.StatusDrone;
import com.example.demo.repository.DroneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DroneService {
    
    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    
    public Drone criarDrone(Drone drone) {
        drone.setStatus(StatusDrone.IDLE);
        drone.setAutonomiaAtualKm(drone.getAutonomiaMaximaKm());
        return droneRepository.save(drone);
    }
    
    public List<Drone> listarDrones() {
        return droneRepository.findAll();
    }
}
