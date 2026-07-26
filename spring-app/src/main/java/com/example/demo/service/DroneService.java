package com.example.demo.service;

import com.example.demo.model.Drone;
import com.example.demo.model.StatusDrone;
import com.example.demo.model.StatusVoo;
import com.example.demo.model.Voo;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.VooRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DroneService {
    
    private final DroneRepository droneRepository;
    private final VooRepository vooRepository;

    public DroneService(DroneRepository droneRepository, VooRepository vooRepository) {
        this.droneRepository = droneRepository;
        this.vooRepository = vooRepository;
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

    // Métricas de viagem/eficiência para o modal "Informações sobre os Drones"
    public Map<String, Object> listarMetricas() {
        List<Voo> voosConcluidos = vooRepository.findAll().stream()
                .filter(v -> v.getStatus() == StatusVoo.CONCLUIDO)
                .toList();

        List<Map<String, Object>> dronesInfo = new ArrayList<>();
        String droneMaisEficiente = null;
        int maiorPontuacao = -1;

        for (Drone drone : droneRepository.findAll()) {
            List<Voo> voosDoDrone = voosConcluidos.stream()
                    .filter(v -> v.getDrone() != null && drone.getId().equals(v.getDrone().getId()))
                    .toList();

            int viagensConcluidas = voosDoDrone.size();
            int pedidosEntregues = voosDoDrone.stream()
                    .mapToInt(v -> v.getPedidos() != null ? v.getPedidos().size() : 0)
                    .sum();
            double tempoTotalMinutos = voosDoDrone.stream()
                    .mapToDouble(v -> v.getTempoTotalEstimadoMinutos() != null ? v.getTempoTotalEstimadoMinutos() : 0.0)
                    .sum();
            double tempoMedioMinutos = viagensConcluidas > 0 ? tempoTotalMinutos / viagensConcluidas : 0.0;

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("codigo", drone.getCodigo());
            info.put("viagensConcluidas", viagensConcluidas);
            info.put("pedidosEntregues", pedidosEntregues);
            info.put("tempoTotalTransitoMinutos", tempoTotalMinutos);
            info.put("tempoMedioViagemMinutos", tempoMedioMinutos);
            dronesInfo.add(info);

            if (pedidosEntregues > maiorPontuacao) {
                maiorPontuacao = pedidosEntregues;
                droneMaisEficiente = drone.getCodigo();
            }
        }

        double tempoMedioGeral = voosConcluidos.stream()
                .mapToDouble(v -> v.getTempoTotalEstimadoMinutos() != null ? v.getTempoTotalEstimadoMinutos() : 0.0)
                .average()
                .orElse(0.0);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("drones", dronesInfo);
        resultado.put("tempoMedioViagemGeralMinutos", tempoMedioGeral);
        resultado.put("droneMaisEficiente", droneMaisEficiente);
        return resultado;
    }
}
