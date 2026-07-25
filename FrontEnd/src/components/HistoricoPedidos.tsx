import { useEffect, useState } from 'react';
import axios from 'axios';
import type { Pedido } from '../types';
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
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    const carregarHistorico = async () => {
      try {
        setCarregando(true);

        const resposta = await axios
          .get('http://localhost:8080/pedidos')
          .catch(() => ({ data: [] }));

        const lista = Array.isArray(resposta.data) ? resposta.data : [];
        const finalizados = lista.filter((pedido: Pedido) => {
          const status = String(pedido.status ?? '').toUpperCase();
          return status === 'ENTREGUE' || status === 'FINALIZADO';
        });

        setPedidos(finalizados);
      } catch (erro) {
        console.error('Erro ao carregar histórico:', erro);
        setPedidos([]);
      } finally {
        setCarregando(false);
      }
    };

    carregarHistorico();
  }, []);

  if (carregando) {
    return <div className="historico-card historico-empty">Carregando histórico...</div>;
  }

  if (!pedidos.length) {
    return <div className="historico-card historico-empty">Nenhum pedido finalizado foi encontrado.</div>;
  }

  return (
    <div className="historico-wrapper">
      <div className="historico-header">
        <div>
          <h2>Histórico de Pedidos</h2>
          <p>Pedidos concluídos e entregues.</p>
        </div>
        <span className="historico-badge">{pedidos.length} finalizados</span>
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
            {pedidos.map((pedido) => (
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
  );
}

export default HistoricoPedidos;
