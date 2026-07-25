import type { Drone, Pedido } from '../types';
import './DashboardMetrics.css';

interface DashboardMetricsProps {
  drones: Drone[];
  pedidos: Pedido[];
  voos?: any[];
}

export default function DashboardMetrics({ drones, pedidos, voos = [] }: DashboardMetricsProps) {
  const dronesDisponiveis = drones.filter(d => d.status === 'IDLE').length;
  const dronesEmVoo = drones.filter(d => d.status === 'CARREGANDO' || d.status === 'EM_TRANSITO' || d.status === 'EM_VOO' || d.status === 'ENTREGANDO' || d.status === 'RETORNANDO').length;
  
  const pedidosEntregues = pedidos.filter(p => p.status === 'ENTREGUE').length;

  let tempoMedioStr = "0 min";
  let droneEficienteStr = "Nenhum";

  if (voos.length > 0) {
    const voosConcluidos = voos.filter(v => v.status === 'CONCLUIDO');
    if (voosConcluidos.length > 0) {
      let totalTempo = 0;
      let totalPedidos = 0;
      let rankingDrones: Record<number, number> = {};

      voosConcluidos.forEach(v => {
        const t = v.tempoTotalEstimadoMinutos || 0;
        const pts = v.pedidos ? v.pedidos.length : 0;
        totalTempo += t;
        totalPedidos += pts;

        if (v.drone && v.drone.id) {
          rankingDrones[v.drone.id] = (rankingDrones[v.drone.id] || 0) + pts;
        }
      });

      if (totalPedidos > 0) {
        tempoMedioStr = (totalTempo / totalPedidos).toFixed(1) + " min";
      }

      if (Object.keys(rankingDrones).length > 0) {
        const melhorId = Object.keys(rankingDrones).sort((a, b) => rankingDrones[Number(b)] - rankingDrones[Number(a)])[0];
        const drone = drones.find(d => String(d.id) === melhorId);
        droneEficienteStr = drone?.codigo ?? `DD${String(melhorId).padStart(2, '0')}`;
      }
    }
  }

  return (
    <div className="metrics-container">
      <div className="metric-card">
        <span className="metric-label">Disponíveis</span>
        <span className="metric-value neon-purple">{dronesDisponiveis}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Em viagem</span>
        <span className="metric-value neon-purple">{dronesEmVoo}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Finalizadas </span>
        <span className="metric-value neon-purple">{pedidosEntregues}</span>
      </div>
            <div className="metric-card">
        <span className="metric-label">Tempo de Entrega Médio</span>
        <span className="metric-value neon-purple">{tempoMedioStr}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Drone Mais Eficiente</span>
        <span className="metric-value glow-text" style={{color: 'var(--primary)'}}>{droneEficienteStr}</span>
      </div>
    </div>
  );
}
