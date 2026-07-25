# Testes Validados — Simulador de Drones

Este documento registra os cenários de negócio e funcionalidades do sistema de drones que foram validados durante o desenvolvimento do projeto.

## 1. Gestão e Identificação da Frota

### [VALIDADO] Padronização de Nomenclatura
- Drones são identificados com o padrão sequencial `DD01`, `DD02`, `DD03`, e assim por diante.
- A identificação é utilizada tanto na frota quanto na métrica de eficiência.

### [VALIDADO] Carga Inicial da Frota
- A inicialização automática da aplicação cria pelo menos 2 drones padrão: `DD01` e `DD02`.
- Os drones iniciam com status `IDLE`.
- A bateria inicial é configurada em 100%.
- A capacidade e a autonomia são mantidas dentro dos limites esperados do desafio.

### [VALIDADO] Bateria Zerada / Indisponibilidade
- Quando a bateria de um drone chega a `0%`, o status do drone é alterado para `INDISPONÍVEL`.
- O drone deixa de ser elegível para novas alocações.

### [VALIDADO] Métrica do Drone Mais Eficiente
- A métrica “Drone Mais Eficiente” exibe o código padronizado do drone, como `DD01`, em vez de um identificador antigo.

## 2. Fila de Entregas e Histórico de Pedidos

### [VALIDADO] Identificação Única
- Os pedidos exibidos na fila usam o código real do pedido, como `#PED-102`.
- A interface não usa mais rótulos genéricos como “Pedido 1”.

### [VALIDADO] Filtragem de Status
- Apenas pedidos com status ativo de fila permanecem visíveis na fila de entregas.
- Pedidos em estados de conclusão saem da fila e não são exibidos como ativos.

### [VALIDADO] Transição para Histórico
- Pedidos com status `ENTREGUE` deixam imediatamente a fila de entregas.
- Esses pedidos passam a ser tratados como parte do histórico de pedidos.

## 3. Alocação e Simulação de Voo

### [VALIDADO] Agrupamento e Capacidade
- O sistema suporta múltiplos pedidos no mesmo drone quando há compatibilidade de capacidade e distância.
- A carga máxima respeita o limite de `2.5 kg`.
- A autonomia respeita o limite de `16 km`.

### [VALIDADO] Visibilidade Dinâmica de Pedidos
- O campo “Pedidos no Voo” é exibido apenas enquanto o drone está em trânsito.
- A informação aparece para os estados `EM_VOO`, `ENTREGANDO` e `RETORNANDO`.
- A informação é ocultada quando o drone retorna a `IDLE`.

### [VALIDADO] Reagendamento por Falta de Bateria
- Se um drone fica indisponível por falta de bateria enquanto ainda possui entregas pendentes, as entregas são reordenadas e transferidas automaticamente para o próximo drone disponível com capacidade suficiente.

## 4. Infraestrutura e Docker

### [VALIDADO] Persistência do Banco
- O ambiente de banco PostgreSQL/PostGIS suporta a criação e manutenção da frota via inicialização do backend.
- A carga inicial e o reset de dados preservam a estrutura necessária para o funcionamento da aplicação.

### [VALIDADO] Integração Full Stack
- O frontend React, executando em `localhost:5173`, comunica corretamente com o backend Spring Boot, executando em `localhost:8080`.
- O fluxo completo entre interface, regras de negócio e persistência foi validado no ecossistema desenvolvido.
