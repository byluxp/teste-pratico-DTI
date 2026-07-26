import { useState, useEffect } from 'react';
import './App.css';
import './Buttons.css';
import { Package, RotateCcw, History, Info } from 'lucide-react';
import MapGrid from './components/MapGrid';
import SidebarQueue from './components/SidebarQueue';
import SidebarDrones from './components/SidebarDrones';
import DashboardMetrics from './components/DashboardMetrics';
import HistoricoPedidos from './components/HistoricoPedidos';
import axios from 'axios';

import type { Pedido, Drone, Obstaculo } from './types';

function App() {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [drones, setDrones] = useState<Drone[]>([]);
  const [obstaculos, setObstaculos] = useState<Obstaculo[]>([]);
  const [activeVoos, setActiveVoos] = useState<any[]>([]);
  const [voos, setVoos] = useState<any[]>([]);
  const [abaAtiva, setAbaAtiva] = useState<'simulador' | 'historico'>('simulador');
  const [modalEspecificacoes, setModalEspecificacoes] = useState(false);

  const fetchDados = async () => {
    try {
      // Usaremos try-catch e ignoraremos falhas se o endpoint não estiver pronto,
      // mas na vida real os endpoints /pedidos, /drones, /obstaculos deveriam existir.
      // O desafio possui /drones.
      
      const resDrones = await axios.get('http://localhost:8080/drones/status').catch(() => ({ data: [] }));
      const resObstaculos = await axios.get('http://localhost:8080/obstaculos').catch(() => ({ data: [] }));
      const resPedidos = await axios.get('http://localhost:8080/pedidos').catch(() => ({ data: [] }));
      const resVoos = await axios.get('http://localhost:8080/entregas/rota').catch(() => ({ data: [] }));

      const pedidosList = Array.isArray(resPedidos.data) ? resPedidos.data : [];
      const voosList = Array.isArray(resVoos.data) ? resVoos.data : [];

      const dronesComPedidos = (Array.isArray(resDrones.data) ? resDrones.data : []).map((drone: any) => {
        const pedidosDoDrone = voosList
          .filter((voo: any) => String(voo?.drone?.id ?? '') === String(drone.id))
          .flatMap((voo: any) => Array.isArray(voo?.pedidos) ? voo.pedidos : [])
          .map((pedido: any) => pedido?.numeroPedido ?? `#${pedido?.id}`)
          .filter(Boolean);

        return {
          ...drone,
          codigo: drone.codigo ?? `DD${drone.id}`,
          pedidosNoVoo: pedidosDoDrone
        };
      });

      setDrones(dronesComPedidos);
      setObstaculos(Array.isArray(resObstaculos.data) ? resObstaculos.data : []);
      setPedidos(pedidosList);
      setVoos(voosList);
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
    } catch (e: any) {
      const message = e?.response?.data?.message || e?.message || 'Erro ao criar pedido';
      window.alert(message);
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
        <div className="header-title-group">
          <h1 className="glow-text">
            <Package size={20} /> Dronelivery
          </h1>
        </div>

        <div className="header-metrics-wrap">
          <DashboardMetrics drones={drones} pedidos={pedidos} voos={voos} />
        </div>

        <div className="header-actions">
          <button className="btn-gerar-mock" onClick={() => setModalEspecificacoes(true)}>
            <Info size={16} /> Especificações
          </button>
          <button className="btn-gerar-mock" onClick={handleReset} title="Resetar Dados">
            <RotateCcw size={16} />
          </button>
        </div>
      </header>

      <div className="abas-navegacao glass-panel">
        <button
          className={`aba-btn ${abaAtiva === 'simulador' ? 'active' : ''}`}
          onClick={() => setAbaAtiva('simulador')}
        >
          Painel de Entregas
        </button>
        <button
          className={`aba-btn ${abaAtiva === 'historico' ? 'active' : ''}`}
          onClick={() => setAbaAtiva('historico')}
        >
          <History size={16} /> Histórico de Pedidos
        </button>
      </div>

      {abaAtiva === 'simulador' ? (
        <>
          <aside className="sidebar glass-panel">
            <div className="sidebar-actions">
              <button className="btn-iniciar-entrega" onClick={handleIniciarEntregas}>
                ▶ Iniciar Entregas
              </button>
              <button className="btn-gerar-mock" onClick={handleGerarPedidosAleatorios}>
                Gerar Pedidos Aleatórios
              </button>
            </div>
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
        </>
      ) : (
        <main className="main-content historico-view glass-panel">
          <HistoricoPedidos />
        </main>
      )}

      {modalEspecificacoes && (
        <div className="modal-overlay" onClick={() => setModalEspecificacoes(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Especificações do Serviço</h3>
            <ul>
              <li><strong>Área de atuação:</strong> Belo Horizonte (MG)</li>
              <li><strong>Distância máxima:</strong> 16 km totais (8 km de ida e 8 km de volta)</li>
              <li><strong>Peso máximo da carga:</strong> 2,5 kg</li>
            </ul>
            <button className="btn-iniciar-entrega" onClick={() => setModalEspecificacoes(false)}>Fechar</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
