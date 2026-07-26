package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.DroneRepository;
import com.example.demo.repository.EntregaRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VooRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SimulacaoService {

    private final DroneRepository droneRepository;
    private final VooRepository vooRepository;
    private final PedidoRepository pedidoRepository;
    private final AlocacaoService alocacaoService;
    private final SseBroadcastService sseBroadcastService;
    private final EntregaRepository entregaRepository;

    public SimulacaoService(DroneRepository droneRepository, VooRepository vooRepository, PedidoRepository pedidoRepository, AlocacaoService alocacaoService, SseBroadcastService sseBroadcastService, EntregaRepository entregaRepository) {
        this.droneRepository = droneRepository;
        this.vooRepository = vooRepository;
        this.pedidoRepository = pedidoRepository;
        this.alocacaoService = alocacaoService;
        this.sseBroadcastService = sseBroadcastService;
        this.entregaRepository = entregaRepository;
    }


    // Duração de cada tick da simulação (ms). Reduzida para 100ms para produzir
    // movimento ainda mais fluido e responsivo no mapa via SSE.
    private static final long TICK_MS = 100;

    // Multiplicador de velocidade da simulação: acelera o ciclo completo (ida + volta)
    // sem alterar o valor real de velocidadeKmH do drone (usado em métricas/relatórios).
    private static final double SPEED_MULTIPLIER = 6.0;

    // Roda a cada 100ms para simular a passagem do tempo com movimento suave e acelerado
    @Scheduled(fixedRate = TICK_MS)
    @Transactional
    public void simularTempo() {
        // A alocação agora é disparada apenas manualmente via endpoint /entregas/despachar
        List<Voo> voosAtivos = vooRepository.findAll().stream()
                .filter(v -> v.getStatus() != StatusVoo.CONCLUIDO)
                .toList();

        for (Voo voo : voosAtivos) {
            Drone drone = voo.getDrone();

            double baseX = drone.getBaseX() != null ? drone.getBaseX() : 0.0;
            double baseY = drone.getBaseY() != null ? drone.getBaseY() : 0.0;
            // Ponto de entrega representativo do voo (último pedido da rota), usado para interpolar a perna de ida
            double destinoX = baseX;
            double destinoY = baseY;
            if (voo.getPedidos() != null && !voo.getPedidos().isEmpty()) {
                Pedido ultimoPedido = voo.getPedidos().get(voo.getPedidos().size() - 1);
                if (ultimoPedido.getCoordenadaX() != null && ultimoPedido.getCoordenadaY() != null) {
                    destinoX = ultimoPedido.getCoordenadaX();
                    destinoY = ultimoPedido.getCoordenadaY();
                }
            }
            // Cada perna (ida/volta) corresponde a metade da distância total prevista da rota
            double distanciaPerna = (voo.getDistanciaTotalPrevistaKm() != null ? voo.getDistanciaTotalPrevistaKm() : 0.0) / 2.0;
            double velocidade = (drone.getVelocidadeKmH() != null && drone.getVelocidadeKmH() > 0 ? drone.getVelocidadeKmH() : 40.0) * SPEED_MULTIPLIER;

            if (voo.getStatus() == StatusVoo.CRIADO) {
                voo.setStatus(StatusVoo.EM_ANDAMENTO);
                voo.setFaseIniciadaEm(LocalDateTime.now());
                drone.setStatus(StatusDrone.EM_VOO);
                drone.setPosX(baseX);
                drone.setPosY(baseY);
                System.out.println("Drone " + drone.getId() + " iniciou o voo " + voo.getId());

                for(Pedido p : voo.getPedidos()) {
                    p.setStatus(StatusPedido.EM_TRANSITO);
                    pedidoRepository.save(p);
                }

                vooRepository.save(voo);
                droneRepository.save(drone);
            } 
            else if (voo.getStatus() == StatusVoo.EM_ANDAMENTO) {
                if (drone.getStatus() == StatusDrone.EM_VOO) {
                    double fracao = calcularFracaoDaPerna(voo.getFaseIniciadaEm(), distanciaPerna, velocidade);
                    drone.setPosX(baseX + (destinoX - baseX) * fracao);
                    drone.setPosY(baseY + (destinoY - baseY) * fracao);

                    if (fracao >= 1.0) {
                        drone.setStatus(StatusDrone.ENTREGANDO);
                        System.out.println("Drone " + drone.getId() + " chegou no destino e está entregando.");
                        // Pedidos permanecem EM_TRANSITO até o ciclo do voo ser concluído (drone retornar à base)
                    }
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.ENTREGANDO) {
                    drone.setStatus(StatusDrone.RETORNANDO);
                    voo.setFaseIniciadaEm(LocalDateTime.now());
                    System.out.println("Drone " + drone.getId() + " finalizou entregas e está retornando.");
                    vooRepository.save(voo);
                    droneRepository.save(drone);
                } 
                else if (drone.getStatus() == StatusDrone.RETORNANDO) {
                    double fracao = calcularFracaoDaPerna(voo.getFaseIniciadaEm(), distanciaPerna, velocidade);
                    drone.setPosX(destinoX + (baseX - destinoX) * fracao);
                    drone.setPosY(destinoY + (baseY - destinoY) * fracao);

                    if (fracao >= 1.0) {
                        double autonomiaRestante = (drone.getAutonomiaAtualKm() != null ? drone.getAutonomiaAtualKm() : 0.0) - (voo.getDistanciaTotalPrevistaKm() != null ? voo.getDistanciaTotalPrevistaKm() : 0.0);
                        drone.setAutonomiaAtualKm(Math.max(autonomiaRestante, 0.0));
                        drone.setStatus(StatusDrone.RECARREGANDO);
                        drone.setPosX(baseX);
                        drone.setPosY(baseY);
                        System.out.println("Drone " + drone.getId() + " retornou à base e está recarregando. Autonomia: " + drone.getAutonomiaAtualKm());

                        voo.setStatus(StatusVoo.CONCLUIDO);
                        voo.setDataHoraChegada(LocalDateTime.now());

                        // Cria o registro agrupado da entrega (viagem) e finaliza os pedidos vinculados a ele
                        Entrega entrega = new Entrega();
                        entrega.setId("ENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                        entrega.setDataHora(LocalDateTime.now());
                        entrega.setDroneId(drone.getId());
                        entrega.setDroneCodigo(drone.getCodigo());
                        entrega.setDistanciaTotal(voo.getDistanciaTotalPrevistaKm());
                        entrega = entregaRepository.save(entrega);

                        // Pedido só é marcado como ENTREGUE quando o ciclo do voo se completa (drone de volta à base)
                        for (Pedido p : voo.getPedidos()) {
                            p.setStatus(StatusPedido.ENTREGUE);
                            p.setDataFinalizacao(LocalDateTime.now());
                            p.setEntrega(entrega);
                            pedidoRepository.save(p);
                        }

                        vooRepository.save(voo);
                    }
                    droneRepository.save(drone);
                }
            }
        }

        // Recarrega gradualmente drones que estão na base até atingir 100% da autonomia
        List<Drone> dronesRecarregando = droneRepository.findAll().stream()
                .filter(d -> d.getStatus() == StatusDrone.RECARREGANDO)
                .toList();

        for (Drone drone : dronesRecarregando) {
            double autonomiaMaxima = drone.getAutonomiaMaximaKm() != null ? drone.getAutonomiaMaximaKm() : 16.0;
            double autonomiaAtual = drone.getAutonomiaAtualKm() != null ? drone.getAutonomiaAtualKm() : 0.0;
            // Taxa de recarga: 0.5% da autonomia por segundo (equivalente aos antigos 5% a cada 10s),
            // agora aplicada proporcionalmente à duração real do tick para manter o tempo total de recarga.
            double incrementoPorTick = autonomiaMaxima * 0.005 * (TICK_MS / 1000.0) * SPEED_MULTIPLIER;
            double novaAutonomia = Math.min(autonomiaAtual + incrementoPorTick, autonomiaMaxima);

            drone.setAutonomiaAtualKm(novaAutonomia);
            if (novaAutonomia >= autonomiaMaxima) {
                drone.setStatus(StatusDrone.IDLE);
                System.out.println("Drone " + drone.getId() + " concluiu a recarga e está IDLE.");
            }
            droneRepository.save(drone);
        }

        // Notifica os clientes conectados via SSE com o estado atualizado da simulação
        sseBroadcastService.broadcast(construirSnapshot());
    }

    // Calcula a fração (0 a 1) já percorrida de uma perna com base no tempo real decorrido desde o início da fase,
    // na distância da perna (km) e na velocidade do drone (km/h) — garante movimento fisicamente consistente.
    private double calcularFracaoDaPerna(LocalDateTime faseIniciadaEm, double distanciaPernaKm, double velocidadeKmH) {
        if (distanciaPernaKm <= 0) {
            return 1.0;
        }
        double duracaoPernaHoras = distanciaPernaKm / velocidadeKmH;
        if (duracaoPernaHoras <= 0 || faseIniciadaEm == null) {
            return 1.0;
        }
        double horasDecorridas = Duration.between(faseIniciadaEm, LocalDateTime.now()).toMillis() / 3_600_000.0;
        return Math.min(1.0, horasDecorridas / duracaoPernaHoras);
    }

    // Snapshot leve (sem proxies/entidades lazy) usado tanto no broadcast periódico quanto na conexão inicial do SSE.
    // @Transactional garante que coleções lazy (ex.: Entrega.pedidos) possam ser acessadas com segurança
    // mesmo quando chamado fora do tick agendado (ex.: na conexão inicial do SSE).
    @Transactional(readOnly = true)
    public Map<String, Object> obterSnapshotAtual() {
        return construirSnapshot();
    }

    private Map<String, Object> construirSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("drones", droneRepository.findAll().stream().map(this::mapDrone).toList());
        snapshot.put("pedidos", pedidoRepository.findAllByOrderByDataCriacaoDesc().stream().map(this::mapPedido).toList());
        snapshot.put("voos", vooRepository.findAll().stream().map(this::mapVoo).toList());
        // Histórico de entregas concluídas é enviado em todo tick para refletir novas entregas
        // imediatamente na aba de Histórico, sem necessidade de polling ou refresh manual.
        snapshot.put("entregas", entregaRepository.findAllByOrderByDataHoraDesc().stream().map(this::mapEntrega).toList());
        return snapshot;
    }

    private Map<String, Object> mapDrone(Drone d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("codigo", d.getCodigo());
        m.put("status", d.getStatus());
        m.put("capacidadeMaximaKg", d.getCapacidadeMaximaKg());
        m.put("autonomiaMaximaKm", d.getAutonomiaMaximaKm());
        m.put("autonomiaAtualKm", d.getAutonomiaAtualKm());
        m.put("velocidadeKmH", d.getVelocidadeKmH());
        m.put("baseX", d.getBaseX());
        m.put("baseY", d.getBaseY());
        m.put("posX", d.getPosX() != null ? d.getPosX() : d.getBaseX());
        m.put("posY", d.getPosY() != null ? d.getPosY() : d.getBaseY());
        return m;
    }

    private Map<String, Object> mapPedido(Pedido p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("numeroPedido", p.getNumeroPedido());
        m.put("coordenadaX", p.getCoordenadaX());
        m.put("coordenadaY", p.getCoordenadaY());
        m.put("peso", p.getPeso());
        m.put("distancia", p.getDistancia());
        m.put("prioridade", p.getPrioridade());
        m.put("status", p.getStatus());
        m.put("dataCriacao", p.getDataCriacao());
        m.put("dataFinalizacao", p.getDataFinalizacao());
        return m;
    }

    private Map<String, Object> mapEntrega(Entrega e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("dataHora", e.getDataHora());
        m.put("droneId", e.getDroneId());
        m.put("droneCodigo", e.getDroneCodigo());
        m.put("distanciaTotal", e.getDistanciaTotal());
        m.put("pedidos", e.getPedidos() != null ? e.getPedidos().stream().map(this::mapPedido).toList() : List.of());
        return m;
    }

    private Map<String, Object> mapVoo(Voo v) {
        Map<String, Object> droneRef = new LinkedHashMap<>();
        droneRef.put("id", v.getDrone() != null ? v.getDrone().getId() : null);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("drone", droneRef);
        m.put("status", v.getStatus());
        m.put("distanciaTotalPrevistaKm", v.getDistanciaTotalPrevistaKm());
        m.put("tempoTotalEstimadoMinutos", v.getTempoTotalEstimadoMinutos());
        m.put("pedidos", v.getPedidos() != null ? v.getPedidos().stream().map(this::mapPedido).toList() : List.of());
        return m;
    }
}
