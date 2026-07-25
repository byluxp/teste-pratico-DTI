import type { Pedido } from '../types';
import './SidebarQueue.css';
import { Package } from 'lucide-react';
import AddPedidoForm from './AddPedidoForm';

interface SidebarQueueProps {
  pedidos: Pedido[];
  onAddPedido: (pedido: any) => Promise<void>;
}

export default function SidebarQueue({ pedidos, onAddPedido }: SidebarQueueProps) {
  return (
    <div className="sidebar-container">
      <h2 className="sidebar-title">Fila de Entregas</h2>
      
      <AddPedidoForm onAdd={onAddPedido} />
      
      <div className="queue-list" style={{ marginTop: '16px' }}>
        {pedidos.length === 0 ? (
          <p className="empty-queue">Nenhum pedido na fila.</p>
        ) : (
          pedidos.map(pedido => {
            const prioridade = pedido.prioridade?.toLowerCase() ?? 'media';
            return (
              <div key={pedido.id} className={`pedido-card priority-${prioridade}`}>
                <div className="pedido-header">
                  <span className="pedido-id"># {pedido.id}</span>
                  <span className="pedido-badge">{pedido.prioridade ?? 'MEDIA'}</span>
                </div>
                <div className="pedido-body">
                  <Package size={16} />
                  <span>Peso: {pedido.peso} kg</span>
                </div>
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
