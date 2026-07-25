package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimulacaoService {

    private final DroneRepository droneRepository;
    private final VooRepository vooRepository;
    private final PedidoRepository pedidoRepository;
    private final AlocacaoService alocacaoService;

    public SimulacaoService(DroneRepository droneRepository, VooRepository vooRepository, PedidoRepository pedidoRepository, AlocacaoService alocacaoService) {
        this.droneRepository = droneRepository;
        this.vooRepository = vooRepository;
        this.pedidoRepository = pedidoRepository;
        this.alocacaoService = alocacaoService;
    }


    // Roda a cada 10 segundos para simular a passagem do tempo
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void simularTempo() {
        // A alocação agora é disparada apenas manualmente via endpoint /entregas/despachar
        List<Voo> voosAtivos = vooRepository.findAll().stream()
                .filter(v -> v.getStatus() != StatusVoo.CONCLUIDO)
                .toList();

        for (Voo voo : voosAtivos) {
            Drone drone = voo.getDrone();

            if (voo.getStatus() == StatusVoo.CRIADO) {
                voo.setStatus(StatusVoo.EM_ANDAMENTO);
                drone.setStatus(StatusDrone.EM_VOO);
                System.out.println("Drone " + drone.getId() + " iniciou o voo " + voo.getId());

                for(Pedido p : voo.getPedidos()) {
                    p.setStatus(StatusPedido.EM_ROTA);
                    pedidoRepository.save(p);
                }

                vooRepository.save(voo);
                droneRepository.save(drone);
            } 
            else if (voo.getStatus() == StatusVoo.EM_ANDAMENTO) {
                if (drone.getStatus() == StatusDrone.EM_VOO) {
                    drone.setStatus(StatusDrone.ENTREGANDO);
                    System.out.println("Drone " + drone.getId() + " chegou no destino e está entregando.");
                    
                    for(Pedido p : voo.getPedidos()) {
                        p.setStatus(StatusPedido.ENTREGUE);
                        pedidoRepository.save(p);
                    }
                    
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.ENTREGANDO) {
                    drone.setStatus(StatusDrone.RETORNANDO);
                    System.out.println("Drone " + drone.getId() + " finalizou entregas e está retornando.");
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.RETORNANDO) {
                    double autonomiaRestante = (drone.getAutonomiaAtualKm() != null ? drone.getAutonomiaAtualKm() : 0.0) - (voo.getDistanciaTotalPrevistaKm() != null ? voo.getDistanciaTotalPrevistaKm() : 0.0);
                    if (autonomiaRestante <= 0.0) {
                        drone.setAutonomiaAtualKm(0.0);
                        drone.setStatus(StatusDrone.INDISPONIVEL);
                    } else {
                        drone.setAutonomiaAtualKm(autonomiaRestante);
                        drone.setStatus(StatusDrone.IDLE);
                    }
                    System.out.println("Drone " + drone.getId() + " retornou à base. Autonomia: " + drone.getAutonomiaAtualKm());
                    
                    voo.setStatus(StatusVoo.CONCLUIDO);
                    voo.setDataHoraChegada(LocalDateTime.now());
                    
                    vooRepository.save(voo);
                    droneRepository.save(drone);
                }
            }
        }
    }
}
