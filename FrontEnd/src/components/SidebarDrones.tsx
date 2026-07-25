import type { Drone } from '../types';
import { Crosshair, BatteryFull, BatteryMedium, BatteryLow } from 'lucide-react';

interface SidebarDronesProps {
  drones: Drone[];
}

export default function SidebarDrones({ drones }: SidebarDronesProps) {
  return (
    <div className="sidebar-section">
      <h2 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#7C3AED', marginBottom: '16px' }}>
        <Crosshair size={20} /> Frota de Drones
      </h2>
      
      <div className="queue-list" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {drones.length === 0 ? (
          <p style={{ color: '#111827', fontSize: '0.9rem' }}>Nenhum drone na frota.</p>
        ) : (
          drones.map(drone => {
            const batteryPct = (drone.autonomiaAtualKm / drone.autonomiaMaximaKm) * 100;
            let BatteryIcon = BatteryFull;
            if (batteryPct < 75) BatteryIcon = BatteryMedium;
            if (batteryPct < 25) BatteryIcon = BatteryLow;
            const status = String(drone.status ?? '').toUpperCase();
            const label = status === 'INDISPONIVEL' ? 'INDISPONÍVEL' : status;
            const pedidosNoVoo = drone.pedidosNoVoo?.length ? drone.pedidosNoVoo.join(', ') : 'Nenhum pedido alocado';

            return (
              <div key={drone.id} className="queue-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                  <strong>{drone.codigo ?? `DD${drone.id}`}</strong>
                  <span className={`status-badge`} style={{
                      backgroundColor: label === 'IDLE' ? 'rgba(0, 240, 255, 0.2)' : label === 'INDISPONÍVEL' ? 'rgba(255, 0, 60, 0.2)' : 'rgba(255, 0, 60, 0.2)',
                      color: label === 'IDLE' ? 'var(--cyan)' : 'var(--red)'
                  }}>
                    {label}
                  </span>
                </div>
                
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  <BatteryIcon size={16} color={batteryPct > 25 ? "var(--cyan)" : "var(--red)"} />
                  <span>{batteryPct.toFixed(0)}% Bateria</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  <span>Capacidade: {drone.capacidadeMaximaKg} kg</span>
                </div>

                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '6px' }}>
                  <span>Pedidos no voo: {pedidosNoVoo}</span>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
