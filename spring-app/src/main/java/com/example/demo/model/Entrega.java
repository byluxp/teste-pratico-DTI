package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// Registro agrupado de uma viagem de entrega concluída (Delivery Trip),
// consolidando todos os pedidos entregues em um único voo/rota.
@Getter
@Setter
@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    private String id;

    private LocalDateTime dataHora;

    private Long droneId;

    private String droneCodigo;

    private Double distanciaTotal;

    @OneToMany(mappedBy = "entrega")
    private List<Pedido> pedidos;

    public Entrega() {
    }
}
