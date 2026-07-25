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
  pedidosNoVoo?: string[];
}

export interface Obstaculo {
  id?: number;
  coordenadaX: number;
  coordenadaY: number;
  raioKm: number;
}
