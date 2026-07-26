import { useEffect, useState } from 'react';
import axios from 'axios';
import type { Entrega } from '../types';
import './HistoricoPedidos.css';

const formatarData = (valor?: string | null) => {
  if (!valor) return '—';

  const data = new Date(valor);
  if (Number.isNaN(data.getTime())) return valor;

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(data);
};

function HistoricoPedidos() {
  const [entregas, setEntregas] = useState<Entrega[]>([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    const carregarHistorico = async () => {
      try {
        setCarregando(true);

        const resposta = await axios
          .get('http://localhost:8080/entregas/historico')
          .catch(() => ({ data: [] }));

        setEntregas(Array.isArray(resposta.data) ? resposta.data : []);
      } catch (erro) {
        console.error('Erro ao carregar histórico:', erro);
        setEntregas([]);
      } finally {
        setCarregando(false);
      }
    };

    carregarHistorico();
  }, []);

  if (carregando) {
    return <div className="historico-card historico-empty">Carregando histórico...</div>;
  }

  if (!entregas.length) {
    return <div className="historico-card historico-empty">Nenhuma entrega concluída foi encontrada.</div>;
  }

  return (
    <div className="historico-wrapper">
      <div className="historico-header">
        <div>
          <h2>Histórico de Pedidos</h2>
          <p>Viagens de entrega concluídas, agrupadas por voo.</p>
        </div>
        <span className="historico-badge">{entregas.length} entregas</span>
      </div>

      {entregas.map((entrega) => (
        <div className="historico-card" key={entrega.id}>
          <div className="historico-header">
            <div>
              <h2>{entrega.id}</h2>
              <p>
                Drone {entrega.droneCodigo ?? entrega.droneId} · {formatarData(entrega.dataHora)} · Distância total: {entrega.distanciaTotal?.toFixed(2) ?? '—'} km
              </p>
            </div>
            <span className="historico-badge">{entrega.pedidos?.length ?? 0} pedido(s)</span>
          </div>

          <div className="historico-table-wrapper">
            <table className="historico-table">
              <thead>
                <tr>
                  <th>Número / ID</th>
                  <th>Data de realização</th>
                  <th>Data de entrega</th>
                  <th>Peso (kg)</th>
                  <th>Distância (km)</th>
                </tr>
              </thead>
              <tbody>
                {(entrega.pedidos ?? []).map((pedido) => (
                  <tr key={pedido.id}>
                    <td>
                      <strong>{pedido.numeroPedido ?? `#${pedido.id}`}</strong>
                    </td>
                    <td>{formatarData(pedido.dataCriacao)}</td>
                    <td>{formatarData(pedido.dataFinalizacao)}</td>
                    <td>{pedido.peso ?? '—'}</td>
                    <td>{pedido.distancia ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  );
}

export default HistoricoPedidos;
