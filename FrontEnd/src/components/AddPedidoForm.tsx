import { useState } from 'react';
import './AddPedidoForm.css';
import { Plus } from 'lucide-react';

interface AddPedidoFormProps {
  onAdd: (pedido: any) => Promise<void>;
}

export default function AddPedidoForm({ onAdd }: AddPedidoFormProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [coordenadaX, setCoordenadaX] = useState('');
  const [coordenadaY, setCoordenadaY] = useState('');
  const [peso, setPeso] = useState('');
  const [prioridade, setPrioridade] = useState('MEDIA');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onAdd({
        coordenadaX: parseFloat(coordenadaX),
        coordenadaY: parseFloat(coordenadaY),
        peso: parseFloat(peso),
        prioridade
      });
      setCoordenadaX('');
      setCoordenadaY('');
      setPeso('');
      setPrioridade('MEDIA');
      setIsOpen(false);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) {
    return (
      <button className="btn-add-pedido" onClick={() => setIsOpen(true)}>
        <Plus size={16} /> Novo Pedido
      </button>
    );
  }

  return (
    <form className="add-pedido-form" onSubmit={handleSubmit}>
      <h3 className="form-title">Criar Pedido</h3>
      
      <div className="input-group">
        <label>Coordenada X (0-100)</label>
        <input type="number" required min="0" max="100" value={coordenadaX} onChange={e => setCoordenadaX(e.target.value)} />
      </div>

      <div className="input-group">
        <label>Coordenada Y (0-100)</label>
        <input type="number" required min="0" max="100" value={coordenadaY} onChange={e => setCoordenadaY(e.target.value)} />
      </div>

      <div className="input-group">
        <label>Peso (kg)</label>
        <input type="number" required step="0.1" min="0.1" value={peso} onChange={e => setPeso(e.target.value)} />
      </div>

      <div className="input-group">
        <label>Prioridade</label>
        <select value={prioridade} onChange={e => setPrioridade(e.target.value)}>
          <option value="BAIXA">Baixa</option>
          <option value="MEDIA">Média</option>
          <option value="ALTA">Alta</option>
        </select>
      </div>

      <div className="form-actions">
        <button type="button" className="btn-cancel" onClick={() => setIsOpen(false)}>Cancelar</button>
        <button type="submit" className="btn-submit" disabled={loading}>
          {loading ? 'Salvando...' : 'Adicionar'}
        </button>
      </div>
    </form>
  );
}
