export interface Pedido {
  id: number;
  numeroPedido?: string;
  coordenadaX?: number;
  coordenadaY?: number;
  peso?: number;
  distancia?: number;
  prioridade?: string;
  status?: string;
  dataCriacao?: string;
  dataFinalizacao?: string;
}

export interface Drone {
  id: number;
  status: string;
  capacidadeMaximaKg: number;
  autonomiaMaximaKm: number;
  autonomiaAtualKm: number;
  velocidadeKmH: number;
  codigo?: string;
  baseX?: number;
  baseY?: number;
  posX?: number;
  posY?: number;
  pedidosNoVoo?: string[];
}

export interface Obstaculo {
  id?: number;
  coordenadaX: number;
  coordenadaY: number;
  raioKm: number;
}

// Registro agrupado de uma viagem de entrega concluída (Delivery Trip)
export interface Entrega {
  id: string;
  dataHora?: string;
  droneId?: number;
  droneCodigo?: string;
  distanciaTotal?: number;
  pedidos: Pedido[];
}
