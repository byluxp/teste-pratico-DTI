# Logs e Prompts de Desenvolvimento da IA

Este documento registra o histórico completo de instruções, prompts e alterações arquiteturais e visuais realizadas com o auxílio da Inteligência Artificial durante a construção e evolução do **Simulador de Entregas por Drone - Dronelivery**.

---

## 🧠 Memória Arquitetural e Regras de Negócio

Durante o processo de desenvolvimento, as seguintes regras de negócio e conceitos técnicos foram consolidados:

1. **Capacidade do Drone e Limites Operacionais:**
   - Cada drone possui autonomia e capacidade de carga otimizadas para cenários urbanos.
   - **Área de Atuação:** Belo Horizonte - MG.
   - **Autonomia/Distância Máxima:** 16 km totais (divididos em raio de até 8 km de ida e 8 km de volta, correspondendo a 80% da autonomia média real de drones de entrega).
   - **Peso Máximo da Carga:** 2.5 kg por viagem.
   - A alocação prioriza pacotes com base em peso, distância e capacidade disponível (Knapsack problem adaptado).

2. **Mapeamento 2D e Navegação:**
   - A malha urbana é representada em um mapa bidimensional interativo com coordenadas X/Y.
   - O Frontend mapeia posições relativas (porcentagens e transformações de pan/zoom) enquanto o Backend calcula distâncias Euclidianas e trajetórias.

3. **Obstáculos e Collision Evasion:**
   - Permite a marcação de zonas de exclusão (obstáculos) no mapa.
   - A animação do drone calcula waypoints dinâmicos no `MapGrid` para contornar ativamente áreas restritas durante o trajeto.

4. **Ciclo de Vida e Máquina de Estados:**
   - O fluxo do drone alterna entre os estados: `IDLE` → `CARREGANDO` → `EM_VOO` → `ENTREGANDO` → `RETORNANDO` → `IDLE`.
   - Na base de origem, há um indicador visual de carregamento com efeito animado.

5. **Persistência de Pedidos e Histórico:**
   - Integração completa com banco de dados PostgreSQL.
   - Cada pedido recebe um código/ID único na criação. Ao concluir a entrega, os dados de execução (`dataCriacao`, `dataFinalizacao`, peso e distância) são finalizados e armazenados para consulta posterior.

---

## 📝 Histórico de Prompts e Evolução do Projeto

### 1. Modelagem Inicial e Regras de Negócio
> **Prompt:** *"Verifique se os requisitos a seguir estão implementados: Simulador de Encomendas em Drone. Regras Básicas: Capacidade: Cada drone suporta até X kg e pode viajar até Y km por carga. Mapeamento: A cidade é uma malha de coordenadas (exemplo: 2D). Sistema de Pedidos: O sistema deve receber pedidos com Localização do cliente (X, Y), Peso do pacote, Prioridade da entrega. O sistema precisa alocar os pacotes nos drones com o menor número de viagens possível, respeitando as regras. Simular bateria, Inserir obstáculos, Calcular tempo total, Criar uma fila de entrega."*

### 2. Interface Gráfica 2D e Elementos Visuais
> **Prompt:** *"Agora vamos criar a representação do frontend simples, simulando visualmente em 2D o drone indo do ponto A ao B, com ícone de bateria, dashboard, funcionalidade de marcar área de obstáculos (estilo seleção do Windows) e o ícone da fila de entregas."*

### 3. Integração Fullstack (Spring Boot + PostgreSQL + React)
> **Prompt:** *"Implementar o fluxo de persistência de Pedidos para que, ao criar um pedido no frontend, ele receba um número/ID único e, ao ser finalizado/entregue, seja salvo no banco PostgreSQL com histórico completo. Atributos: id, numeroPedido, peso, distancia, status, dataCriacao e dataFinalizacao."*

### 4. Navegação Inteligente e Desvio de Obstáculos
> **Prompt:** *"Revise para que o drone dê a volta no obstáculo."*

### 5. Dashboard e Métricas de Desempenho
> **Prompt:** *"Crie a funcionalidade de dashboard. Criar uma visualização simples com: Quantidade de entregas realizadas, Tempo médio por entrega, Drone mais eficiente, Mapa das entregas."*

### 6. Histórico de Pedidos Finalizados
> **Prompt:** *"Faça no frontend uma aba onde posso acompanhar os pedidos que já foram realizados e dados como finalizados com as opções de id do pedido, data que foi realizado, características como peso e distância."*

### 7. Reformulação do Layout e Estilização (Dark Mode no Mapa & Tema Claro Geral)
> **Prompt:** *"Altere o componente MapGrid para ter o fundo escuro (#111827) e linhas de grid discretas (#1F2937), mantendo o contraste dos elementos sobre ele. O restante do aplicativo deve usar fundo claro e botões em tom de roxo (#7C3AED)."*

### 8. Ajuste de Alinhamento e Header Contínuo
> **Prompt:** *"Alinhar título 'Dronelivery' no canto superior esquerdo e transformar os cards de métricas em uma barra superior contínua, retangular e unificada (sem o aspecto de botões/cards arredondados e soltos)."*

### 9. Tipografia Geométrica e Design Quadrado
> **Prompt:** *"Atualizar a tipografia do projeto para a fonte 'Chakra Petch' (estilo quadrado/geométrico) e zerar o border-radius de TODOS os elementos visuais da barra superior, botões e cards de métricas, deixando a estrutura com bordas retas."*

### 10. Reorganização de Fluxo, Especificações e Animações no Mapa
> **Prompt:** *"Reformular a estrutura visual para um estilo borderless (integrado ao fundo), reordenar os botões de ação ('Iniciar Entregas' agrupado ao 'Novo Pedido' acima de 'Gerar Pedidos Aleatórios'), criar modal de Especificações do Serviço (regras de BH, limites de 16km totais e 2.5kg), adicionar marcador de drone em movimento no trajeto e ícone de recarga com efeito pulso na base do mapa."*

### 11. Validação do Build e Suíte de Testes
> **Status:** Aprovado.
> - **Backend (Spring Boot):** 10/10 testes unitários executados com 0 falhas e 0 erros.
> - **Frontend (React + Vite/TS):** Processo de build e verificação de tipos concluídos com sucesso (0 erros de compilação).

### 12. Reset de Sessão, Ciclo de Bateria, Priorização e Botão de Métricas
> **Prompt:** *"Atue como Desenvolvedor Full Stack (Spring Boot + React + TypeScript) para resolver 5 problemas/melhorias no sistema: 1) Ajustar Docker/Spring Boot para zerar o banco de dados e vir limpo a cada inicialização; 2) Inicializar o drone 'DD02' e priorizar o 'DD01' na fila de alocação (acionando o DD02 apenas se DD01 estiver ocupado/sem bateria); 3) Simular recarga automática gradual da bateria do drone ao retornar para a base e validar autonomia antes da decolagem; 4) Restringir a geração de pedidos aleatórios para nunca ultrapassar 2.5 kg e 8 km de raio (16 km totais); 5) Criar um terceiro botão 'Informações sobre os Drones' na interface exibindo tempo médio de viagem e indicação do Drone Mais Eficiente em um modal."*

### 13. Ajustes Finais de Interface, Navegação e Animações do Mapa
> **Prompt:** *"Atue como Desenvolvedor Full Stack para realizar 6 correções na interface e regras: 1) Mover o registro de pedidos finalizados para que entrem no histórico e contador APENAS após a conclusão real do voo; 2) Remover 'Tempo Médio' e 'Drone Eficiente' da barra superior e consolidá-los dentro do modal de informações; 3) Posicionar o botão 'Informações sobre os Drones' ao lado da aba 'Histórico de Pedidos'; 4) Ajustar a simulação de bateria para carregar gradualmente de +5% em +5%; 5) Separar a posição inicial dos drones no mapa (DD01 e DD02 em bases distintas) e garantir que cada um retorne à sua base de origem; 6) Reduzir o tamanho dos botões/setas de controle do mapa."*

### 14. Atualização em Tempo Real da Animação via WebSockets / SSE
> **Prompt:** *"Atue como Desenvolvedor Full Stack para substituir o mecanismo de atualização por polling HTTP no Frontend por um sistema de transmissão em tempo real via SSE (Server-Sent Events) ou WebSockets, garantindo que a movimentação e a animação dos drones no mapa 2D ocorram de forma fluida e instantânea à medida que o estado muda no Backend."*

### 15. Calibração da Simulação no Backend e Transições Suaves no Frontend
> **Prompt:** *"Atue como Desenvolvedor Full Stack para calibrar a frequência da simulação no Backend e adicionar interpolação/transição suave (CSS transition/requestAnimationFrame) na renderização das coordenadas do drone no Frontend, garantindo um deslocamento visual contínuo e sem saltos no mapa."*

### 16. Barra de Progresso, Ajuste na Fila/Histórico, Limite de Carga e Entidades de Entrega
> **Prompt:** *"Atue como Desenvolvedor Full Stack para implementar 4 ajustes de regras e dados: 1) Adicionar uma barra de progresso visual para cada entrega, simulando em tempo real a distância percorrida da base até o destino final (desconsiderar o trajeto de retorno); 2) Corrigir a persistência dos pedidos para que 'Pedidos em Voo' exiba estritamente os pedidos em trajeto ativo, transferindo os dados de pedidos finalizados exclusivamente para o Histórico de Pedidos; 3) Criar uma regra de validação na alocação de carga para proibir que o peso somado de múltiplos pedidos agrupados em um mesmo voo ultrapasse a capacidade máxima de 2,5 kg do drone; 4) Criar a entidade 'Entrega' no banco/memória com ID aleatório único, agrupando e armazenando todos os dados dos pedidos atendidos naquela viagem específica (seja individual ou múltiplo) para registro consolidado no Histórico."*

### 17. Otimização do Tempo de Voo, Indicadores de Distância, Limpeza de UI e Histórico Imediato
> **Prompt:** *"Atue como Desenvolvedor Full Stack para realizar 4 melhorias operacionais e de interface: 1) Reduzir significativamente a duração do tempo de viagem e de retorno dos drones na simulação, agilizando a demonstração para os avaliadores; 2) Exibir a distância total/restante a ser percorrida nos cards da 'Fila de Entregas' e 'Pedidos em Voo'; 3) Remover o card/modal informativo que surge ao clicar em 'Gerar Pedidos Aleatórios', inserindo-os diretamente na fila em silêncio; 4) Atualizar o status do pedido para 'Entregue' e registrá-lo imediatamente no Histórico assim que o drone alcançar a coordenada de destino."*

### 18. Refinamento de Status, Validação de Progresso, Limpeza da Frota e Despacho por Bateria
> **Prompt:** *"Atue como Desenvolvedor Full Stack para aplicar 5 correções e regras de negócio avançadas: 1) Transicionar o status do pedido para 'Entregue' e removê-lo da seção 'Pedidos em Voo' assim que a etapa 'Entregando' for ativada no destino; 2) Validar e corrigir a renderização da barra de progresso de entrega para garantir funcionamento simultâneo e consistente em todos os pedidos em voo; 3) Limpar os detalhes do drone no painel 'Frota de Drones', exibindo apenas os pedidos em transporte ativo no momento (remover históricos e pedidos concluídos desta aba mesmo durante o retorno/recarga); 4) Enviar os dados da entrega para o Histórico de forma imediata após o cumprimento, sem aguardar o retorno e a recarga total do drone; 5) Implementar validação de consumo de bateria por peso/distância: se o nível atual suportar o trajeto com segurança, autorizar o despacho imediato de pedidos de Prioridade ALTA antes de aguardar a recarga completa (manter recarga prévia obrigatória apenas para prioridades Média e Baixa)."*
---

## 🛠️ Resumo das Alterações Realizadas no Projeto

### Backend (Java Spring Boot)
- **Model / Entity:** Criação da entidade JPA `Pedido` mapeada para a tabela PostgreSQL, contendo campos para ID, número do pedido, peso, distância, status e timestamps (`dataCriacao`, `dataFinalizacao`).
- **Repository:** Interface `PedidoRepository` via `JpaRepository` para operações de CRUD.
- **Service & Controller:**
  - Endpoint `POST /api/pedidos` para registro de novos pedidos com geração automática de código único.
  - Endpoint `PUT /api/pedidos/{id}/finalizar` para registrar a entrega concluída e salvar timestamp de finalização.
  - Endpoint `GET /api/pedidos` para consulta do histórico de entregas.
  - Endpoint de métricas da frota para expor tempo médio de viagem e cálculo do drone mais eficiente.
- **Gestão de Bateria & Alocação:**
  - Implementação do robô/agendador de recarga automática de bateria e validação de autonomia mínima no `AlocacaoService`.
  - Priorização na ordem de seleção de drones (`DD01` primeiro, transbordo para `DD02`).

### Frontend (React + TypeScript)
- **Aba de Histórico (`HistoricoPedidos.tsx`):** Visualização em tabela/cards dedicada aos pedidos finalizados, listando ID, data de criação, data de entrega, peso e distância.
- **Visual e Tipografia (`Chakra Petch`):** Importação da fonte geométrica via Google Fonts e padronização visual com bordas retas (`border-radius: 0px`).
- **Navegação e Layout:**
  - Header reorganizado com alinhamento do título "Dronelivery".
  - Barra de métricas contínua ("Drones Disponíveis", "Drones em Voo", "Tempo Médio", "Entregas Concluídas", "Drone Eficiente") integrada sem cards isolados.
  - Reordenação da sidebar de pedidos ("Iniciar Entregas" e "Novo Pedido" posicionados acima de "Gerar Pedidos Aleatórios").
  - Estilo *Borderless*: Eliminação de caixas brancas com bordas pesadas para integração fluida com o fundo da página.
- **Modal de Informações e Especificações:**
  - Pop-up "Especificações do Serviço" (regras técnicas de BH, raio de 8km ida + 8km volta, peso máximo de 2.5kg).
  - Terceiro botão/modal "Informações sobre os Drones" trazendo dados consolidados de eficiência e tempo médio por viagem.
- **Gerador de Pedidos Aleatórios:**
  - Aplicação de teto máximo de 2.5 kg por pacote e 8 km de raio por pedido gerado.
- **Mapa e Animação de Drones (`MapGrid` / `DroneMarker`):**
  - Estilização escura (*Dark Mode* `#111827`) focada exclusivamente no `MapGrid`.
  - Ícone de drone acompanhando o vetor de movimento durante a simulação de voo.
  - Ícone de base de carregamento (*Charger*) posicionado no ponto de origem com efeito visual animado de pulso/neon de recarga.