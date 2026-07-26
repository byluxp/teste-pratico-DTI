import type { Drone, Pedido } from '../types';
import './DashboardMetrics.css';

interface DashboardMetricsProps {
  drones: Drone[];
  pedidos: Pedido[];
}

export default function DashboardMetrics({ drones, pedidos }: DashboardMetricsProps) {
  const dronesDisponiveis = drones.filter(d => d.status === 'IDLE').length;
  const dronesEmVoo = drones.filter(d => d.status === 'CARREGANDO' || d.status === 'EM_TRANSITO' || d.status === 'EM_VOO' || d.status === 'ENTREGANDO' || d.status === 'RETORNANDO').length;
  
  const pedidosEntregues = pedidos.filter(p => p.status === 'ENTREGUE').length;

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
    </div>
  );
}
