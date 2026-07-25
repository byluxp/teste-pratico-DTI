package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class Obstaculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coordenadas do centro do obstáculo
    private Double coordenadaX;
    private Double coordenadaY;

    // Raio de exclusão do obstáculo
    private Double raioKm;

    public Obstaculo() {
    }
}
