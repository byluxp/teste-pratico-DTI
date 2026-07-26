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
                    // Pedidos permanecem EM_ROTA até o ciclo do voo ser concluído (drone retornar à base)
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.ENTREGANDO) {
                    drone.setStatus(StatusDrone.RETORNANDO);
                    System.out.println("Drone " + drone.getId() + " finalizou entregas e está retornando.");
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.RETORNANDO) {
                    double autonomiaRestante = (drone.getAutonomiaAtualKm() != null ? drone.getAutonomiaAtualKm() : 0.0) - (voo.getDistanciaTotalPrevistaKm() != null ? voo.getDistanciaTotalPrevistaKm() : 0.0);
                    drone.setAutonomiaAtualKm(Math.max(autonomiaRestante, 0.0));
                    drone.setStatus(StatusDrone.RECARREGANDO);
                    System.out.println("Drone " + drone.getId() + " retornou à base e está recarregando. Autonomia: " + drone.getAutonomiaAtualKm());
                    
                    voo.setStatus(StatusVoo.CONCLUIDO);
                    voo.setDataHoraChegada(LocalDateTime.now());

                    // Pedido só é marcado como ENTREGUE quando o ciclo do voo se completa (drone de volta à base)
                    for (Pedido p : voo.getPedidos()) {
                        p.setStatus(StatusPedido.ENTREGUE);
                        p.setDataFinalizacao(LocalDateTime.now());
                        pedidoRepository.save(p);
                    }
                    
                    vooRepository.save(voo);
                    droneRepository.save(drone);
                }
            }
        }

        // Recarrega gradualmente drones que estão na base até atingir 100% da autonomia
        List<Drone> dronesRecarregando = droneRepository.findAll().stream()
                .filter(d -> d.getStatus() == StatusDrone.RECARREGANDO)
                .toList();

        for (Drone drone : dronesRecarregando) {
            double autonomiaMaxima = drone.getAutonomiaMaximaKm() != null ? drone.getAutonomiaMaximaKm() : 16.0;
            double autonomiaAtual = drone.getAutonomiaAtualKm() != null ? drone.getAutonomiaAtualKm() : 0.0;
            double incrementoPorTick = autonomiaMaxima * 0.05; // recarrega 5% da autonomia a cada tick, simulando carregamento contínuo e suave
            double novaAutonomia = Math.min(autonomiaAtual + incrementoPorTick, autonomiaMaxima);

            drone.setAutonomiaAtualKm(novaAutonomia);
            if (novaAutonomia >= autonomiaMaxima) {
                drone.setStatus(StatusDrone.IDLE);
                System.out.println("Drone " + drone.getId() + " concluiu a recarga e está IDLE.");
            }
            droneRepository.save(drone);
        }
    }
}
