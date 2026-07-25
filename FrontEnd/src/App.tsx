import { useState, useEffect } from 'react';
import './App.css';
import './Buttons.css';
import { Zap, RotateCcw } from 'lucide-react';
import MapGrid from './components/MapGrid';
import SidebarQueue from './components/SidebarQueue';
import SidebarDrones from './components/SidebarDrones';
import DashboardMetrics from './components/DashboardMetrics';
import axios from 'axios';

import type { Pedido, Drone, Obstaculo } from './types';

function App() {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [drones, setDrones] = useState<Drone[]>([]);
  const [obstaculos, setObstaculos] = useState<Obstaculo[]>([]);
  const [activeVoos, setActiveVoos] = useState<any[]>([]);
  const [voos, setVoos] = useState<any[]>([]);

  const fetchDados = async () => {
    try {
      // Usaremos try-catch e ignoraremos falhas se o endpoint não estiver pronto,
      // mas na vida real os endpoints /pedidos, /drones, /obstaculos deveriam existir.
      // O desafio possui /drones.
      
      const resDrones = await axios.get('http://localhost:8080/drones/status').catch(() => ({ data: [] }));
      setDrones(Array.isArray(resDrones.data) ? resDrones.data : []);
      
      const resObstaculos = await axios.get('http://localhost:8080/obstaculos').catch(() => ({ data: [] }));
      setObstaculos(Array.isArray(resObstaculos.data) ? resObstaculos.data : []);
      
      const resPedidos = await axios.get('http://localhost:8080/pedidos').catch(() => ({ data: [] }));
      setPedidos(Array.isArray(resPedidos.data) ? resPedidos.data : []);

      const resVoos = await axios.get('http://localhost:8080/entregas/rota').catch(() => ({ data: [] }));
      setVoos(Array.isArray(resVoos.data) ? resVoos.data : []);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchDados();
    const interval = setInterval(fetchDados, 5000); // Polling a cada 5s
    return () => clearInterval(interval);
  }, []);

  const handleAddObstaculo = async (obs: Obstaculo) => {
    try {
      const res = await axios.post('http://localhost:8080/obstaculos', obs);
      setObstaculos(prev => [...prev, res.data]);
    } catch (e) {
      console.error("Erro ao adicionar obstáculo:", e);
    }
  };

  const handleAddPedido = async (pedido: any) => {
    try {
      const res = await axios.post('http://localhost:8080/pedidos', pedido);
      setPedidos(prev => [...prev, res.data]);
    } catch (e) {
      console.error("Erro ao criar pedido:", e);
    }
  };

  const handleDeleteObstaculo = async (id: number) => {
    try {
      await axios.delete(`http://localhost:8080/obstaculos/${id}`);
      setObstaculos(prev => prev.filter(obs => obs.id !== id));
    } catch (e) {
      console.error("Erro ao deletar obstáculo:", e);
    }
  };

  const handleGerarPedidosAleatorios = async () => {
    for (let i = 0; i < 5; i++) {
      await handleAddPedido({
        coordenadaX: Math.floor(Math.random() * 80) + 10,
        coordenadaY: Math.floor(Math.random() * 80) + 10,
        peso: Math.floor(Math.random() * 10) + 1,
        prioridade: ['ALTA', 'MEDIA', 'BAIXA'][Math.floor(Math.random() * 3)]
      });
    }
  };

  const handleIniciarEntregas = async () => {
    try {
      const res = await axios.post('http://localhost:8080/entregas/despachar');
      if (res.data && res.data.length > 0) {
        setActiveVoos(res.data);
      }
      fetchDados(); // refresh immediate
    } catch (e) {
      console.error("Erro ao despachar:", e);
    }
  };

  const handleReset = async () => {
    try {
      await axios.post('http://localhost:8080/reset');
      setPedidos([]);
      setObstaculos([]);
      fetchDados();
    } catch (e) {
      console.error("Erro ao resetar:", e);
    }
  };

  return (
    <div className="app-container">
      <header className="dashboard-header glass-panel">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <h1 className="glow-text" style={{ display: 'flex', gap: '8px' }}>
            <Zap color="var(--cyan)" /> Drone simulator
          </h1>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button className="btn-iniciar-entrega" onClick={handleIniciarEntregas}>
               ▶ Iniciar Entregas (Despachar)
            </button>
            <button className="btn-gerar-mock" style={{ padding: '8px' }} onClick={handleReset} title="Resetar Dados">
              <RotateCcw size={16} />
            </button>
          </div>
        </div>
        <DashboardMetrics drones={drones} pedidos={pedidos} voos={voos} />
      </header>

      <aside className="sidebar glass-panel">
        <button className="btn-gerar-mock" onClick={handleGerarPedidosAleatorios}>
          Gerar 5 Pedidos Mock
        </button>
        <SidebarQueue pedidos={pedidos} onAddPedido={handleAddPedido} />
      </aside>

      <main className="main-content glass-panel">
        <MapGrid 
          obstaculos={obstaculos} 
          drones={drones} 
          pedidos={pedidos}
          activeVoos={activeVoos}
          onAddObstaculo={handleAddObstaculo} 
          onDeleteObstaculo={handleDeleteObstaculo}
        />
      </main>

      <aside className="sidebar right-sidebar glass-panel">
        <SidebarDrones drones={drones} />
      </aside>
    </div>
  );
}

export default App;
