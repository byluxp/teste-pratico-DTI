package com.example.demo.config;

import com.example.demo.model.Drone;
import com.example.demo.model.StatusDrone;
import com.example.demo.repository.DroneRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DroneRepository droneRepository;

    public DataSeeder(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedDrone();
    }

    public void seedDrone() {
        if (droneRepository.count() == 0) {
            criarDronePadrao("DD01", 2.5, 16.0, 0.0, 0.0);
            criarDronePadrao("DD02", 2.5, 16.0, 5.0, 0.0);
        }
    }

    private void criarDronePadrao(String codigo, double capacidade, double autonomia, double baseX, double baseY) {
        Drone drone = new Drone();
        drone.setCodigo(codigo);
        drone.setCapacidadeMaximaKg(capacidade);
        drone.setAutonomiaMaximaKm(autonomia);
        drone.setAutonomiaAtualKm(autonomia);
        drone.setVelocidadeKmH(40.0);
        drone.setStatus(StatusDrone.IDLE);
        drone.setBaseX(baseX);
        drone.setBaseY(baseY);
        droneRepository.save(drone);
    }
}
