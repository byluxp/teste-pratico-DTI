import { useState, useEffect } from 'react';
import './App.css';
import './Buttons.css';
import { Package, RotateCcw, History, Info, Gauge } from 'lucide-react';
import MapGrid from './components/MapGrid';
import SidebarQueue from './components/SidebarQueue';
import SidebarDrones from './components/SidebarDrones';
import DashboardMetrics from './components/DashboardMetrics';
import HistoricoPedidos from './components/HistoricoPedidos';
import axios from 'axios';

import type { Pedido, Drone, Obstaculo } from './types';
import { useSimulacaoStream } from './hooks/useSimulacaoStream';

function App() {
  const { drones: dronesStream, pedidos: pedidosStream, voos, entregas } = useSimulacaoStream();
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [drones, setDrones] = useState<Drone[]>([]);
  const [obstaculos, setObstaculos] = useState<Obstaculo[]>([]);
  const [activeVoos, setActiveVoos] = useState<any[]>([]);
  const [abaAtiva, setAbaAtiva] = useState<'simulador' | 'historico'>('simulador');
  const [modalEspecificacoes, setModalEspecificacoes] = useState(false);
  const [modalInfoDrones, setModalInfoDrones] = useState(false);
  const [metricasDrones, setMetricasDrones] = useState<any>(null);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(timer);
  }, [toast]);

  const fetchObstaculos = async () => {
    try {
      const res = await axios.get('http://localhost:8080/obstaculos');
      setObstaculos(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error("Erro ao buscar obstáculos:", e);
    }
  };

  useEffect(() => {
    fetchObstaculos();
  }, []);

  // Drones e pedidos agora chegam em tempo real via SSE (useSimulacaoStream),
  // substituindo o polling HTTP anterior. O campo `pedidosNoVoo` já vem calculado pelo backend
  // (Requisito 3), contendo APENAS os pedidos ainda em trânsito (EM_TRANSITO) de voos ativos —
  // é recalculado a cada tick e nunca preserva pedidos já entregues no card do drone.
  useEffect(() => {
    const dronesComPedidos = dronesStream.map((drone: any) => ({
      ...drone,
      codigo: drone.codigo ?? `DD${drone.id}`,
      pedidosNoVoo: Array.isArray(drone.pedidosNoVoo) ? drone.pedidosNoVoo : []
    }));

    setDrones(dronesComPedidos);
  }, [dronesStream]);

  useEffect(() => {
    setPedidos(pedidosStream);
  }, [pedidosStream]);

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
    const DISTANCIA_MAX_KM = 8; // raio máximo, garante ida + volta <= 16 km
    for (let i = 0; i < 5; i++) {
      const angulo = Math.random() * (Math.PI / 2); // quadrante positivo (0-90°)
      const raio = Math.random() * DISTANCIA_MAX_KM;
      const coordenadaX = parseFloat((raio * Math.cos(angulo)).toFixed(2));
      const coordenadaY = parseFloat((raio * Math.sin(angulo)).toFixed(2));
      const peso = parseFloat((Math.random() * (2.5 - 0.1) + 0.1).toFixed(2));
      const prioridade = ['ALTA', 'MEDIA', 'BAIXA'][Math.floor(Math.random() * 3)];

      await handleAddPedido({ coordenadaX, coordenadaY, peso, prioridade });
    }
    setToast('5 pedidos aleatórios gerados e adicionados à fila.');
  };

  const handleIniciarEntregas = async () => {
    try {
      const res = await axios.post('http://localhost:8080/entregas/despachar');
      if (res.data && res.data.length > 0) {
        setActiveVoos(res.data);
      }
    } catch (e) {
      console.error("Erro ao despachar:", e);
    }
  };

  const handleAbrirInfoDrones = async () => {
    try {
      const res = await axios.get('http://localhost:8080/drones/metricas');
      setMetricasDrones(res.data);
    } catch (e) {
      console.error("Erro ao buscar métricas dos drones:", e);
    }
    setModalInfoDrones(true);
  };

  const handleReset = async () => {
    try {
      await axios.post('http://localhost:8080/reset');
      setPedidos([]);
      setObstaculos([]);
      fetchObstaculos();
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
          <DashboardMetrics drones={drones} pedidos={pedidos} />
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
        <button className="aba-btn info-drones-btn" onClick={handleAbrirInfoDrones}>
          <Gauge size={16} /> Informações sobre os Drones
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
            <SidebarQueue pedidos={pedidos} drones={drones} voos={voos} onAddPedido={handleAddPedido} />
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
          <HistoricoPedidos entregas={entregas} />
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

      {modalInfoDrones && (
        <div className="modal-overlay" onClick={() => setModalInfoDrones(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Informações sobre os Drones</h3>
            <p><strong>Tempo médio de viagem (geral):</strong> {Number(metricasDrones?.tempoMedioViagemGeralMinutos ?? 0).toFixed(1)} min</p>
            <p><strong>Drone mais eficiente:</strong> {metricasDrones?.droneMaisEficiente ?? 'Nenhum'}</p>
            <ul>
              {(metricasDrones?.drones ?? []).map((d: any) => (
                <li key={d.codigo}>
                  <strong>{d.codigo}:</strong> {d.viagensConcluidas} viagens concluídas, {d.pedidosEntregues} pedidos entregues, tempo médio {Number(d.tempoMedioViagemMinutos ?? 0).toFixed(1)} min
                </li>
              ))}
            </ul>
            <button className="btn-iniciar-entrega" onClick={() => setModalInfoDrones(false)}>Fechar</button>
          </div>
        </div>
      )}

      {toast && <div className="toast-notification">{toast}</div>}
    </div>
  );
}

export default App;
