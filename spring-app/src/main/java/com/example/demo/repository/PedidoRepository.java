package com.example.demo.repository;

import com.example.demo.model.Pedido;
import com.example.demo.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByStatusOrderByPrioridadeDescDataCriacaoAsc(StatusPedido status);
    List<Pedido> findByStatus(StatusPedido status);
    boolean existsByNumeroPedido(String numeroPedido);
    List<Pedido> findAllByOrderByDataCriacaoDesc();
}
