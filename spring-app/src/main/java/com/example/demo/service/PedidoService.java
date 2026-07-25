package com.example.demo.service;

import com.example.demo.model.Pedido;
import com.example.demo.model.StatusPedido;
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

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido criarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo");
        }

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
