export interface Pedido {
  id: number;
  coordenadaX: number;
  coordenadaY: number;
  peso: number;
  prioridade: string;
  status: string;
}

export interface Drone {
  id: number;
  status: string;
  capacidadeMaximaKg: number;
  autonomiaMaximaKm: number;
  autonomiaAtualKm: number;
  velocidadeKmH: number;
}

export interface Obstaculo {
  id?: number;
  coordenadaX: number;
  coordenadaY: number;
  raioKm: number;
}
