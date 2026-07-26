package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoServiceTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private VooRepository vooRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private AlocacaoService alocacaoService;

    @InjectMocks
    private SimulacaoService simulacaoService;

    private Drone drone;
    private Voo voo;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        drone = new Drone();
        drone.setId(1L);
        drone.setAutonomiaAtualKm(50.0);
        drone.setAutonomiaMaximaKm(50.0);

        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setStatus(StatusPedido.EM_ROTA);

        voo = new Voo();
        voo.setId(100L);
        voo.setDrone(drone);
        voo.setPedidos(Collections.singletonList(pedido));
        voo.setDistanciaTotalPrevistaKm(20.0);
    }

    @Test
    void deveTransitarDeCriadoParaEmAndamento() {
        voo.setStatus(StatusVoo.CRIADO);
        drone.setStatus(StatusDrone.CARREGANDO);

        when(vooRepository.findAll()).thenReturn(List.of(voo));

        simulacaoService.simularTempo();

        assertEquals(StatusVoo.EM_ANDAMENTO, voo.getStatus());
        assertEquals(StatusDrone.EM_VOO, drone.getStatus());
        assertEquals(StatusPedido.EM_ROTA, pedido.getStatus());

        verify(vooRepository).save(voo);
        verify(droneRepository).save(drone);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void deveTransitarDeEmVooParaEntregandoSemFinalizarPedido() {
        voo.setStatus(StatusVoo.EM_ANDAMENTO);
        drone.setStatus(StatusDrone.EM_VOO);

        when(vooRepository.findAll()).thenReturn(List.of(voo));

        simulacaoService.simularTempo();

        assertEquals(StatusDrone.ENTREGANDO, drone.getStatus());
        // Pedido só deve ser finalizado quando o voo concluir o ciclo (drone de volta à base)
        assertEquals(StatusPedido.EM_ROTA, pedido.getStatus());

        verify(droneRepository).save(drone);
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void deveTransitarDeEntregandoParaRetornando() {
        voo.setStatus(StatusVoo.EM_ANDAMENTO);
        drone.setStatus(StatusDrone.ENTREGANDO);

        when(vooRepository.findAll()).thenReturn(List.of(voo));

        simulacaoService.simularTempo();

        assertEquals(StatusDrone.RETORNANDO, drone.getStatus());

        verify(droneRepository).save(drone);
    }

    @Test
    void deveTransitarDeRetornandoParaRecarregandoEFinalizarPedido() {
        voo.setStatus(StatusVoo.EM_ANDAMENTO);
        drone.setStatus(StatusDrone.RETORNANDO);

        when(vooRepository.findAll()).thenReturn(List.of(voo));

        simulacaoService.simularTempo();

        assertEquals(StatusDrone.RECARREGANDO, drone.getStatus());
        assertEquals(StatusVoo.CONCLUIDO, voo.getStatus());
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
        assertEquals(10L, pedido.getId());
        
        // Autonomia inicial era 50, viagem era 20
        assertEquals(30.0, drone.getAutonomiaAtualKm());

        verify(vooRepository).save(voo);
        verify(droneRepository, atLeastOnce()).save(drone);
        verify(pedidoRepository).save(pedido);
    }
}
