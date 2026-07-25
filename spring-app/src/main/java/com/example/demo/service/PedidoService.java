package com.example.demo.service;

import com.example.demo.model.Drone;
import com.example.demo.model.Pedido;
import com.example.demo.model.PrioridadePedido;
import com.example.demo.model.StatusPedido;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DroneRepository droneRepository;

    public PedidoService(PedidoRepository pedidoRepository, DroneRepository droneRepository) {
        this.pedidoRepository = pedidoRepository;
        this.droneRepository = droneRepository;
    }

    public Pedido criarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo");
        }
        if (pedido.getCoordenadaX() == null || pedido.getCoordenadaY() == null) {
            throw new IllegalArgumentException("Coordenadas do cliente são obrigatórias");
        }
        if (pedido.getCoordenadaX() < 0 || pedido.getCoordenadaX() > 100 || pedido.getCoordenadaY() < 0 || pedido.getCoordenadaY() > 100) {
            throw new IllegalArgumentException("Coordenadas devem estar entre 0 e 100");
        }
        if (pedido.getPeso() == null || pedido.getPeso() <= 0) {
            throw new IllegalArgumentException("Peso do pacote é obrigatório e deve ser positivo");
        }
        if (pedido.getPrioridade() == null) {
            pedido.setPrioridade(PrioridadePedido.MEDIA);
        }

        double capacidadeMaxima = obterCapacidadeMaxima();
        if (pedido.getPeso() > capacidadeMaxima) {
            throw new IllegalArgumentException("Pedido excede a capacidade máxima do drone: " + capacidadeMaxima + " kg");
        }

        double distanciaBase = Math.hypot(pedido.getCoordenadaX(), pedido.getCoordenadaY());
        double distanciaTotalPrevista = distanciaBase * 2.0;
        double autonomiaMaxima = obterAutonomiaMaxima();
        if (distanciaTotalPrevista > autonomiaMaxima) {
            throw new IllegalArgumentException("Pedido excede a distância máxima da carga: " + autonomiaMaxima + " km");
        }

        pedido.setDistancia(distanciaBase);
        pedido.setNumeroPedido(gerarNumeroPedido());
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setDataFinalizacao(null);
        if (pedido.getStatus() == null) {
            pedido.setStatus(StatusPedido.PENDENTE);
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido finalizarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com id " + id));

        pedido.setStatus(StatusPedido.ENTREGUE);
        pedido.setDataFinalizacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByDataCriacaoDesc();
    }

    private double obterCapacidadeMaxima() {
        return droneRepository.findAll().stream()
                .map(Drone::getCapacidadeMaximaKg)
                .filter(capacidade -> capacidade != null)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(2.5);
    }

    private double obterAutonomiaMaxima() {
        return droneRepository.findAll().stream()
                .map(Drone::getAutonomiaMaximaKm)
                .filter(autonomia -> autonomia != null)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(16.0);
    }

    private String gerarNumeroPedido() {
        String numeroPedido;
        int tentativas = 0;

        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String sequencia = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
            numeroPedido = "PED-" + timestamp + "-" + sequencia;
            tentativas++;
        } while (pedidoRepository.existsByNumeroPedido(numeroPedido) && tentativas < 10);

        if (tentativas >= 10) {
            throw new IllegalStateException("Não foi possível gerar um número único para o pedido");
        }

        return numeroPedido;
    }
}
