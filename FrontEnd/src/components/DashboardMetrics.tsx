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
        droneEficienteStr = `Drone #${melhorId}`;
      }
    }
  }

  return (
    <div className="metrics-container">
      <div className="metric-card">
        <span className="metric-label">Drones Disponíveis</span>
        <span className="metric-value neon-green">{dronesDisponiveis}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Drones em Voo</span>
        <span className="metric-value neon-cyan">{dronesEmVoo}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Tempo Médio/Entrega</span>
        <span className="metric-value neon-yellow">{tempoMedioStr}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Entregas Concluídas</span>
        <span className="metric-value neon-purple">{pedidosEntregues}</span>
      </div>
      <div className="metric-card">
        <span className="metric-label">Drone Mais Eficiente</span>
        <span className="metric-value glow-text" style={{color: 'var(--green)'}}>{droneEficienteStr}</span>
      </div>
    </div>
  );
}
