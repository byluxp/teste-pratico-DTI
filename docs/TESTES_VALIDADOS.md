# Testes Validados — Simulador de Drones

Este documento registra os cenários de negócio, regras físicas, lógicas de alocação e funcionalidades do sistema de drones que foram validados durante o desenvolvimento do projeto.

---

## 1. Gestão, Identificação e Bases da Frota

### [VALIDADO] Padronização de Nomenclatura e Bases Distintas
- Drones são identificados com o padrão sequencial `DD01`, `DD02`, `DD03`, etc.
- A inicialização automática garante que `DD01` e `DD02` iniciem em bases de origem distintas no mapa 2D, e que cada drone retorne estritamente à sua base de origem após a entrega.

### [VALIDADO] Carga Inicial, Status e Bateria
- Drones iniciam com status `IDLE` e bateria em 100%.
- Quando a bateria atinge `0%`, o status do drone muda para `INDISPONÍVEL`, tornando-o inelegível para novas alocações.
- Recarga gradual de bateria implementada de +5% em +5% no Backend.

### [VALIDADO] Métrica do Drone Mais Eficiente e Métricas Globais
- Métrica "Drone Mais Eficiente" exibe a identificação padronizada (`DD01`, `DD02`).
- As métricas de tempo médio de viagem e eficiência consideram apenas os dados de voos finalizados com sucesso.

---

## 2. Regras Físicas, Capacidade e Geração Aleatória de Pedidos

### [VALIDADO] Restrição Rígida de Carga Única e Agrupada
- **Limite Individual e Agrupado:** A carga total transportada em uma única viagem — seja para um único pedido ou múltiplos pedidos agrupados — é limitada em no máximo **2.5 kg**.
- **Validação de Agrupamento:** Se 2 ou mais pedidos forem alocados na mesma viagem, o sistema valida que a soma de seus pesos não ultrapasse 2,5 kg. Caso ultrapasse, o agrupamento é negado.

### [VALIDADO] Raio de Alcance e Autonomia Distância
- O raio máximo por pedido é restrito a **8 km** (garantindo 8 km de ida + 8 km de volta, totalizando o limite de autonomia de **16 km**).

### [VALIDADO] Geração Aleatória em Conformidade com os Limites
- A função de geração de pedidos aleatórios no Backend gera pacotes estritamente dentro das capacidades operacionais: peso individual de até 2.5 kg e distância dentro do raio máximo de 8 km da base.

---

## 3. Despacho Inteligente e Consumo de Bateria por Prioridade

### [VALIDADO] Cálculo de Consumo de Bateria por Peso/Distância
- O sistema calcula previamente o consumo estimado de bateria para a viagem completa com base no peso da carga e distância total.

### [VALIDADO] Despacho Prioritário por Nível de Prioridade e Bateria
- **Prioridade ALTA:** Se a bateria atual for suficiente para cobrir a viagem (ida, entrega e retorno seguro), o drone é despachado **imediatamente**, priorizando a viagem antes de realizar a recarga completa.
- **Prioridades Média e Baixa:** Exigem obrigatoriamente a recarga total (100%) do drone antes de iniciar um novo despacho.

### [VALIDADO] Reagendamento e Transbordo por Falta de Bateria
- Se um drone fica indisponível ou descarrega antes da alocação, entregas pendentes são reordenadas e transferidas automaticamente para o próximo drone disponível (`DD01` -> `DD02`).

---

## 4. Fila de Entregas, Transição e Histórico de Pedidos / Entregas

### [VALIDADO] Mudança Instantânea para 'Entregue'
- O status do pedido transiciona para `ENTREGUE` imediatamente assim que o drone aciona a etapa `ENTREGANDO` no ponto de destino.
- O pedido é removido da lista de pedidos em voo assim que entregue, sem depender do tempo de retorno do drone à base.

### [VALIDADO] Criação da Entidade "Entrega" e Armazenamento no Histórico
- **Entidade Entrega:** É gerado um identificador/código único aleatório por viagem (`Entrega`). Esta entidade consolida e armazena todos os dados de todos os pedidos atendidos naquela rota (suportando entregas individuais ou agrupadas).
- **Persistência Imediata:** Assim que o pedido alcança o status `ENTREGUE`, os dados da entrega/pedido são persistidos no banco de dados e disponibilizados no Histórico, desvinculando-se do ciclo de recarga do drone.

### [VALIDADO] Isolamento de Dados na Frota vs. Histórico
- O painel de detalhes do drone na frota exibe **apenas os pedidos em voo ativo no momento**.
- Pedidos com status `ENTREGUE` ou durante o trajeto de retorno não persistem na visualização do drone, permanecendo exclusivamente armazenados no Histórico.

---

## 5. Simulação de Voo, Animação e Comunicação em Tempo Real

### [VALIDADO] Otimização da Velocidade de Simulação
- O tempo de viagem e de retorno dos drones foi calibrado e reduzido no Backend para permitir testes ágeis e validação completa do fluxo sem longos tempos de espera.

### [VALIDADO] Comunicação via SSE / WebSockets
- Substituição do polling HTTP por transmissão de eventos em tempo real (SSE/WebSockets), garantindo atualização instantânea do status do Backend para o Frontend.

### [VALIDADO] Transição e Animação Flutuante
- A posição do drone e a barra de progresso do trajeto de ida atualizam de forma contínua e validada para múltiplos pedidos simultâneos em trânsito.

---

## 6. Infraestrutura e Persistência

### [VALIDADO] Persistência Relacional com PostgreSQL / PostGIS
- O banco de dados PostgreSQL sustenta as tabelas e relacionamentos entre `drones`, `pedidos` e `entregas`, garantindo rastreabilidade histórica e integridade relacional.

### [VALIDADO] Integração Full Stack
- Comunicação de alta performance e baixa latência entre o Backend Spring Boot (`localhost:8080`) e o Frontend React (`localhost:5173`), cobrindo o ciclo completo: cadastro -> alocação -> simulação -> entrega imediata -> histórico.