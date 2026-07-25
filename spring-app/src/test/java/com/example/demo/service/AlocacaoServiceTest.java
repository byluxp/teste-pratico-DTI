package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import com.example.demo.repository.ObstaculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlocacaoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private VooRepository vooRepository;

    @Mock
    private ObstaculoRepository obstaculoRepository;

    @InjectMocks
    private AlocacaoService alocacaoService;

    private Drone droneDisponivel;
    private Pedido pedido1;
    private Pedido pedido2;

    @BeforeEach
    void setUp() {
        droneDisponivel = new Drone();
        droneDisponivel.setId(1L);
        droneDisponivel.setStatus(StatusDrone.IDLE);
        droneDisponivel.setCapacidadeMaximaKg(10.0);
        droneDisponivel.setAutonomiaMaximaKm(50.0);
        droneDisponivel.setAutonomiaAtualKm(50.0);
        droneDisponivel.setVelocidadeKmH(40.0);

        pedido1 = new Pedido();
        pedido1.setId(10L);
        pedido1.setPeso(2.0);
        pedido1.setCoordenadaX(3.0);
        pedido1.setCoordenadaY(4.0); // Distancia até a base (0,0) = 5. Ida e volta = 10
        pedido1.setPrioridade(PrioridadePedido.ALTA);
        pedido1.setStatus(StatusPedido.PENDENTE);

        pedido2 = new Pedido();
        pedido2.setId(20L);
        pedido2.setPeso(9.0); // Somado ao pedido 1 excede a capacidade do drone
        pedido2.setCoordenadaX(0.0);
        pedido2.setCoordenadaY(10.0); // Distancia até a base = 10
        pedido2.setPrioridade(PrioridadePedido.MEDIA);
        pedido2.setStatus(StatusPedido.PENDENTE);
    }

    @Test
    void naoDeveFazerNadaSeNaoHouverDrones() {
        when(droneRepository.findAll()).thenReturn(Collections.emptyList());

        alocacaoService.alocarPedidos();

        verify(pedidoRepository, never()).findByStatusOrderByPrioridadeDescDataCriacaoAsc(any());
        verify(vooRepository, never()).save(any());
    }

    @Test
    void deveAlocarPedidoUnicoSeRespeitarCapacidadeEAutonomia() {
        when(droneRepository.findAll()).thenReturn(List.of(droneDisponivel));
        when(pedidoRepository.findByStatusOrderByPrioridadeDescDataCriacaoAsc(StatusPedido.PENDENTE))
                .thenReturn(new java.util.ArrayList<>(List.of(pedido1)));
        when(vooRepository.save(any(Voo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alocacaoService.alocarPedidos();

        ArgumentCaptor<Voo> vooCaptor = ArgumentCaptor.forClass(Voo.class);
        verify(vooRepository).save(vooCaptor.capture());
        
        Voo vooSalvo = vooCaptor.getValue();
        assertEquals(droneDisponivel, vooSalvo.getDrone());
        assertEquals(1, vooSalvo.getPedidos().size());
        assertTrue(vooSalvo.getPedidos().contains(pedido1));
        
        // A distancia é Base -> Pedido1 -> Base = 5 + 5 = 10
        assertEquals(10.0, vooSalvo.getDistanciaTotalPrevistaKm());
        assertEquals(2.0, vooSalvo.getPesoTotalCarregadoKg());
        assertEquals(StatusVoo.CRIADO, vooSalvo.getStatus());
        
        assertEquals(StatusDrone.CARREGANDO, droneDisponivel.getStatus());
        assertEquals(StatusPedido.ALOCADO, pedido1.getStatus());
    }

    @Test
    void deveRespeitarCapacidadeDoDroneNaoAlocandoPedidosQueExcedamPeso() {
        when(droneRepository.findAll()).thenReturn(List.of(droneDisponivel));
        when(pedidoRepository.findByStatusOrderByPrioridadeDescDataCriacaoAsc(StatusPedido.PENDENTE))
                .thenReturn(new java.util.ArrayList<>(List.of(pedido1, pedido2)));
        when(vooRepository.save(any(Voo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alocacaoService.alocarPedidos();

        ArgumentCaptor<Voo> vooCaptor = ArgumentCaptor.forClass(Voo.class);
        verify(vooRepository).save(vooCaptor.capture());

        Voo vooSalvo = vooCaptor.getValue();
        
        // Pedido 1 (2.0kg) entra, Pedido 2 (9.0kg) seria 11.0kg, que estoura a capacidade de 10.0kg.
        // Como temos apenas 1 drone disponivel, ele levará só o pedido 1.
        assertEquals(1, vooSalvo.getPedidos().size());
        assertTrue(vooSalvo.getPedidos().contains(pedido1));
        assertFalse(vooSalvo.getPedidos().contains(pedido2));
    }

    @Test
    void deveAgruparMultiplosPedidosNoMesmoVooSeEstiveremDentroDosLimites() {
        Pedido pedido3 = new Pedido();
        pedido3.setId(30L);
        pedido3.setPeso(3.0);
        pedido3.setCoordenadaX(6.0);
        pedido3.setCoordenadaY(8.0);
        // Distancia do (3,4) para (6,8): sqrt((3-6)^2 + (4-8)^2) = sqrt(9 + 16) = 5
        // Distancia do (6,8) para Base (0,0): 10
        // Total da rota: Base->P1(5) + P1->P3(5) + P3->Base(10) = 20. (Autonomia é 50, então cabe)
        pedido3.setPrioridade(PrioridadePedido.MEDIA);
        pedido3.setStatus(StatusPedido.PENDENTE);

        when(droneRepository.findAll()).thenReturn(List.of(droneDisponivel));
        when(pedidoRepository.findByStatusOrderByPrioridadeDescDataCriacaoAsc(StatusPedido.PENDENTE))
                .thenReturn(new java.util.ArrayList<>(List.of(pedido1, pedido3)));
        when(vooRepository.save(any(Voo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alocacaoService.alocarPedidos();

        ArgumentCaptor<Voo> vooCaptor = ArgumentCaptor.forClass(Voo.class);
        verify(vooRepository).save(vooCaptor.capture());

        Voo vooSalvo = vooCaptor.getValue();
        
        assertEquals(2, vooSalvo.getPedidos().size());
        assertTrue(vooSalvo.getPedidos().contains(pedido1));
        assertTrue(vooSalvo.getPedidos().contains(pedido3));
        
        assertEquals(20.0, vooSalvo.getDistanciaTotalPrevistaKm());
        assertEquals(5.0, vooSalvo.getPesoTotalCarregadoKg());
    }
}
