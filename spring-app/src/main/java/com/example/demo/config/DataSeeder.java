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
            Drone drone = new Drone();
            drone.setCapacidadeMaximaKg(200.0);
            drone.setAutonomiaMaximaKm(1000.0);
            drone.setAutonomiaAtualKm(1000.0);
            drone.setVelocidadeKmH(40.0);
            drone.setStatus(StatusDrone.IDLE);
            droneRepository.save(drone);
        }
    }
}
