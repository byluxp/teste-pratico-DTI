package com.example.demo.service;

import com.example.demo.model.Drone;
import com.example.demo.model.Pedido;
import com.example.demo.model.PrioridadePedido;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DroneRepository droneRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveRecusarPedidoQueExcedeCapacidadeMaximaDisponivel() {
        Drone drone = new Drone();
        drone.setCapacidadeMaximaKg(2.5);
        when(droneRepository.findAll()).thenReturn(List.of(drone));

        Pedido pedido = new Pedido();
        pedido.setPeso(3.0);
        pedido.setCoordenadaX(10.0);
        pedido.setCoordenadaY(10.0);
        pedido.setPrioridade(PrioridadePedido.ALTA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(pedido));

        assertTrue(ex.getMessage().contains("capacidade"));
        verify(pedidoRepository, never()).save(any());
    }
}
