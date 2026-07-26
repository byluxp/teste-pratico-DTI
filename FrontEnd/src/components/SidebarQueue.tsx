import type { Pedido, Drone } from '../types';
import './SidebarQueue.css';
import { Package, Plane } from 'lucide-react';
import AddPedidoForm from './AddPedidoForm';

interface SidebarQueueProps {
  pedidos: Pedido[];
  drones: Drone[];
  voos: any[];
  onAddPedido: (pedido: any) => Promise<void>;
}

// Calcula o progresso (0-100%) da perna de ida com base na posição atual do drone (SSE),
// usando a base do drone como origem e a coordenada do pedido como destino.
// Retorna null quando não há voo/drone associado, e "visivel: false" durante o retorno (RETORNANDO).
function calcularProgresso(pedido: Pedido, voos: any[], drones: Drone[]): { percentual: number; visivel: boolean } | null {
  const voo = voos.find((v: any) => Array.isArray(v?.pedidos) && v.pedidos.some((p: any) => p.id === pedido.id));
  if (!voo || !voo.drone) return null;

  const drone = drones.find(d => String(d.id) === String(voo.drone.id));
  if (!drone) return null;

  const status = String(drone.status ?? '').toUpperCase();
  if (status === 'RETORNANDO') {
    return { percentual: 100, visivel: false };
  }

  const baseX = drone.baseX ?? 0;
  const baseY = drone.baseY ?? 0;
  const destX = pedido.coordenadaX ?? 0;
  const destY = pedido.coordenadaY ?? 0;
  const curX = drone.posX ?? baseX;
  const curY = drone.posY ?? baseY;

  const distanciaTotal = Math.hypot(destX - baseX, destY - baseY);
  const distanciaPercorrida = Math.hypot(curX - baseX, curY - baseY);
  const percentual = distanciaTotal > 0 ? Math.min(100, Math.max(0, (distanciaPercorrida / distanciaTotal) * 100)) : 100;

  return { percentual, visivel: true };
}

export default function SidebarQueue({ pedidos, drones, voos, onAddPedido }: SidebarQueueProps) {
  // Fila de espera: pedidos ainda não despachados (aguardando alocação/decolagem)
  const pedidosNaFila = pedidos.filter((pedido) => {
    const status = String(pedido.status ?? '').toUpperCase();
    return status === 'PENDENTE' || status === 'ALOCADO';
  });

  // Voos ativos: exclusivamente pedidos EM_TRANSITO (removidos daqui assim que o voo é concluído/ENTREGUE)
  const pedidosEmVoo = pedidos.filter((pedido) => String(pedido.status ?? '').toUpperCase() === 'EM_TRANSITO');

  return (
    <div className="sidebar-container">
      <h2 className="sidebar-title">Fila de Entregas</h2>
      
      <AddPedidoForm onAdd={onAddPedido} />
      
      <div className="queue-list" style={{ marginTop: '16px' }}>
        {pedidosNaFila.length === 0 ? (
          <p className="empty-queue">Nenhum pedido na fila.</p>
        ) : (
          pedidosNaFila.map(pedido => {
            const prioridade = pedido.prioridade?.toLowerCase() ?? 'media';
            return (
              <div key={pedido.id} className={`pedido-card priority-${prioridade}`}>
                <div className="pedido-header">
                  <span className="pedido-id">{pedido.numeroPedido ?? `#${pedido.id}`}</span>
                  <span className="pedido-badge">{pedido.prioridade ?? 'MEDIA'}</span>
                </div>
                <div className="pedido-body">
                  <Package size={16} />
                  <span>Peso: {pedido.peso} kg</span>
                </div>
                <div className="pedido-body">
                  <span>Distância até o destino: {pedido.distancia?.toFixed(2) ?? '—'} km</span>
                </div>
                <div className="pedido-footer">
                  Status: {pedido.status}
                </div>
              </div>
            );
          })
        )}
      </div>

      <h2 className="sidebar-title" style={{ marginTop: '24px' }}>Pedidos em Voo</h2>
      <div className="queue-list">
        {pedidosEmVoo.length === 0 ? (
          <p className="empty-queue">Nenhum pedido em voo no momento.</p>
        ) : (
          pedidosEmVoo.map(pedido => {
            const prioridade = pedido.prioridade?.toLowerCase() ?? 'media';
            const progresso = calcularProgresso(pedido, voos, drones);
            return (
              <div key={pedido.id} className={`pedido-card priority-${prioridade}`}>
                <div className="pedido-header">
                  <span className="pedido-id">{pedido.numeroPedido ?? `#${pedido.id}`}</span>
                  <span className="pedido-badge">{pedido.prioridade ?? 'MEDIA'}</span>
                </div>
                <div className="pedido-body">
                  <Plane size={16} />
                  <span>Peso: {pedido.peso} kg</span>
                </div>
                <div className="pedido-body">
                  <span>Distância até o destino: {pedido.distancia?.toFixed(2) ?? '—'} km</span>
                </div>
                {progresso && progresso.visivel && (
                  <div className="progress-bar-wrapper">
                    <div className="progress-bar-track">
                      <div className="progress-bar-fill" style={{ width: `${progresso.percentual}%` }} />
                    </div>
                    <span className="progress-bar-label">{progresso.percentual.toFixed(0)}% até o destino</span>
                  </div>
                )}
                <div className="pedido-footer">
                  Status: {pedido.status}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
