import { useEffect, useRef, useState } from 'react';

const API_BASE = 'http://localhost:8080';

interface SimulacaoSnapshot {
  drones: any[];
  pedidos: any[];
  voos: any[];
  entregas: any[];
}

/**
 * Substitui o polling HTTP por uma conexão Server-Sent Events (SSE) para
 * receber, em tempo real, o estado de drones/pedidos/voos da simulação.
 * O EventSource nativo do browser já reconecta automaticamente em caso de queda.
 */
export function useSimulacaoStream() {
  const [drones, setDrones] = useState<any[]>([]);
  const [pedidos, setPedidos] = useState<any[]>([]);
  const [voos, setVoos] = useState<any[]>([]);
  const [entregas, setEntregas] = useState<any[]>([]);
  const [conectado, setConectado] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    const eventSource = new EventSource(`${API_BASE}/api/v1/simulacao/stream`);
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => setConectado(true);

    eventSource.addEventListener('simulacao-update', (event: MessageEvent) => {
      try {
        const snapshot: SimulacaoSnapshot = JSON.parse(event.data);
        setDrones(Array.isArray(snapshot.drones) ? snapshot.drones : []);
        setPedidos(Array.isArray(snapshot.pedidos) ? snapshot.pedidos : []);
        setVoos(Array.isArray(snapshot.voos) ? snapshot.voos : []);
        setEntregas(Array.isArray(snapshot.entregas) ? snapshot.entregas : []);
      } catch (e) {
        console.error('Erro ao processar evento SSE da simulação:', e);
      }
    });

    eventSource.onerror = () => {
      // EventSource já tenta reconectar sozinho; apenas refletimos o estado.
      setConectado(false);
    };

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, []);

  return { drones, pedidos, voos, entregas, conectado };
}
