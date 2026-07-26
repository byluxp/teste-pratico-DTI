package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class Voo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @OneToMany(mappedBy = "voo", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;

    // Distância total calculada (ida, percurso entre clientes e volta)
    private Double distanciaTotalPrevistaKm;

    // Tempo total estimado em minutos baseado na velocidade do drone e distância
    private Double tempoTotalEstimadoMinutos;

    // Peso total carregado (para validar contra a capacidade do drone)
    private Double pesoTotalCarregadoKg;

    @Enumerated(EnumType.STRING)
    private StatusVoo status;

    private LocalDateTime dataHoraSaida;
    private LocalDateTime dataHoraChegada;

    // Timestamp de início da etapa/fase atual (EM_VOO ou RETORNANDO), usado para
    // interpolar a posição do drone com base na velocidade real (km/h) e na distância da perna.
    private LocalDateTime faseIniciadaEm;

    public Voo() {
    }

}
