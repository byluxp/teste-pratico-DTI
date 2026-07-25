package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlocacaoService {

    private final PedidoRepository pedidoRepository;
    private final DroneRepository droneRepository;
    private final VooRepository vooRepository;
    private final com.example.demo.repository.ObstaculoRepository obstaculoRepository;

    public AlocacaoService(PedidoRepository pedidoRepository, DroneRepository droneRepository, VooRepository vooRepository, com.example.demo.repository.ObstaculoRepository obstaculoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.droneRepository = droneRepository;
        this.vooRepository = vooRepository;
        this.obstaculoRepository = obstaculoRepository;
    }


    @Transactional
    public List<Voo> alocarPedidos() {
        List<Voo> voosCriados = new ArrayList<>();
        List<Drone> dronesDisponiveis = droneRepository.findAll().stream()
                .filter(d -> d.getStatus() == StatusDrone.IDLE)
                .toList();

        if (dronesDisponiveis.isEmpty()) {
            return voosCriados;
        }

        List<Pedido> pedidosPendentes = pedidoRepository.findByStatusOrderByPrioridadeDescDataCriacaoAsc(StatusPedido.PENDENTE);
        List<Obstaculo> obstaculos = obstaculoRepository.findAll();

        for (Drone drone : dronesDisponiveis) {
            if (pedidosPendentes.isEmpty()) break;

            List<Pedido> pedidosAlocados = new ArrayList<>();
            double pesoAtual = 0.0;
            double xAtual = 0.0;
            double yAtual = 0.0;
            double distanciaTotalPrevista = 0.0;

            for (Pedido pedido : new ArrayList<>(pedidosPendentes)) {
                if (pesoAtual + pedido.getPeso() > drone.getCapacidadeMaximaKg()) {
                    continue;
                }

                double distanciaParaPedido = calcularDistanciaComObstaculos(xAtual, yAtual, pedido.getCoordenadaX(), pedido.getCoordenadaY(), obstaculos);
                double distanciaDeVolta = calcularDistanciaComObstaculos(pedido.getCoordenadaX(), pedido.getCoordenadaY(), 0.0, 0.0, obstaculos);
                
                double distanciaDeVoltaAntiga = calcularDistanciaComObstaculos(xAtual, yAtual, 0.0, 0.0, obstaculos);
                double novaDistanciaTotal = distanciaTotalPrevista - distanciaDeVoltaAntiga + distanciaParaPedido + distanciaDeVolta;

                if (novaDistanciaTotal <= drone.getAutonomiaAtualKm()) {
                    pedidosAlocados.add(pedido);
                    pesoAtual += pedido.getPeso();
                    distanciaTotalPrevista = novaDistanciaTotal;
                    xAtual = pedido.getCoordenadaX();
                    yAtual = pedido.getCoordenadaY();
                    pedidosPendentes.remove(pedido); 
                }
            }

            if (!pedidosAlocados.isEmpty()) {
                Voo voo = new Voo();
                voo.setDrone(drone);
                voo.setPedidos(pedidosAlocados);
                voo.setPesoTotalCarregadoKg(pesoAtual);
                voo.setDistanciaTotalPrevistaKm(distanciaTotalPrevista);

                if (drone.getVelocidadeKmH() != null && drone.getVelocidadeKmH() > 0) {
                    voo.setTempoTotalEstimadoMinutos((distanciaTotalPrevista / drone.getVelocidadeKmH()) * 60.0);
                }

                voo.setStatus(StatusVoo.CRIADO);
                voo.setDataHoraSaida(LocalDateTime.now());

                voo = vooRepository.save(voo);

                for (Pedido p : pedidosAlocados) {
                    p.setStatus(StatusPedido.ALOCADO);
                    p.setVoo(voo);
                    pedidoRepository.save(p);
                }

                drone.setStatus(StatusDrone.CARREGANDO);
                droneRepository.save(drone);
                voosCriados.add(voo);
            }
        }
        return voosCriados;
    }

    private double calcularDistanciaComObstaculos(double x1, double y1, double x2, double y2, List<Obstaculo> obstaculos) {
        double distanciaReta = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double distanciaTotal = distanciaReta;

        for (Obstaculo obs : obstaculos) {
            if (houverInterseccao(x1, y1, x2, y2, obs)) {
                // Penalidade para desviar do obstáculo (meia circunferência + margem)
                distanciaTotal += (Math.PI * obs.getRaioKm());
            }
        }
        return distanciaTotal;
    }

    private boolean houverInterseccao(double x1, double y1, double x2, double y2, Obstaculo obs) {
        double cx = obs.getCoordenadaX();
        double cy = obs.getCoordenadaY();
        double r = obs.getRaioKm();

        // Cálculo da distância do centro do obstáculo até o segmento de reta
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lineLengthSq = dx * dx + dy * dy;

        if (lineLengthSq == 0) {
            return Math.sqrt(Math.pow(cx - x1, 2) + Math.pow(cy - y1, 2)) <= r;
        }

        double t = Math.max(0, Math.min(1, ((cx - x1) * dx + (cy - y1) * dy) / lineLengthSq));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;

        double distanceSq = Math.pow(cx - projX, 2) + Math.pow(cy - projY, 2);
        return distanceSq <= r * r;
    }
}
