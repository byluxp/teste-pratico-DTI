# 🚁 Simulador de Entregas por Drone

> **Desenvolvido por:** Lucila Cardoso

Uma solução logística full-stack projetada para automação, simulação e monitoramento de entregas com drones em tempo real. O sistema gerencia a alocação de pedidos, calcula rotas otimizadas em um mapa 2D, desvia de zonas de exclusão aérea, e aplica algoritmos inteligentes de gestão de bateria e agendamento por prioridade.

---

## 💡 O que o Programa Faz

* **Monitoramento em Tempo Real:** Visualização dinâmica dos drones (`DD01`, `DD02`, etc.) percorrendo o mapa 2D, com transmissão de telemetria via Server-Sent Events (SSE) / WebSockets.
* **Alocação Inteligente e Multi-pedidos:** Agrupa e distribui pedidos automaticamente entre os drones respeitando limites de carga e autonomia.
* **Indicador de Progresso de Viagem:** Acompanhamento visual da distância percorrida e barra de progresso individual para cada pedido durante o trajeto de ida até a entrega.
* **Gestão Dinâmica de Bateria:** Cálculo de consumo estimado de energia por rota/peso com estratégia de recarga sob demanda para emergências.
* **Histórico Abrangente de Entregas:** Registro consolidado que mapeia a entidade `Entrega` (viagem), identificando o drone responsável e os pedidos transportados na missão.
* **Geração Automática de Pedidos:** Criador de demanda em conformidade com as regras geográficas e restrições técnicas reais.

---

## 📌 Visão Geral do Projeto

| Camada | Tecnologia Principal | Descrição |
| :--- | :--- | :--- |
| **Frontend** | React 18 + Vite + TypeScript | Interface do usuário, renderização do mapa e progresso |
| **Backend** | Java 21 + Spring Boot 3.x | Regras de negócio, algoritmo de alocação e SSE/Sockets |
| **Banco de Dados** | PostgreSQL 16 + PostGIS | Geoprocessamento, entidades relacionais e persistência |
| **Containers** | Docker & Docker Compose | Padronização e orquestração da infraestrutura |

---

## ⚙️ Decisões Importantes de Engenharia & Regras de Negócio

### 1. Barra de Carregamento / Progresso da Viagem
- O Frontend exibe uma barra de progresso visual em tempo real para cada pedido em voo.
- O progresso calcula estritamente o trajeto de **ida (Base → Destino do Pedido)**.
- Ao alcançar 100% (etapa `ENTREGANDO`), o pedido passa para o status `ENTREGUE` e é imediatamente persistido no histórico, enquanto o drone inicia o trajeto de retorno à sua base.

### 2. Regra de Prioridade & Carregamento Otimizado de Bateria
- **Prioridade ALTA (Despacho Expresso):** O sistema calcula a energia necessária para cobrir a viagem (ida + entrega + retorno seguro). Se a bateria atual for suficiente, o drone **NÃO aguarda a recarga completa (100%)** — ele recarrega apenas o estritamente necessário para cumprir a rota com segurança e é despachado imediatamente.
- **Prioridades Média e Baixa:** Exigem obrigatoriamente a recarga total (100%) do drone antes do despacho.

### 3. Consolidação de Entregas e Histórico
- **Entidade `Entrega`:** Cada voo/viagem gera um registro de `Entrega` com código único.
- Se o drone transporta 1 ou mais pedidos na mesma viagem, todos são vinculados a essa mesma `Entrega`.
- O Histórico registra: a `Entrega`, o código do **Drone responsável** (ex: `DD01`), a lista de **Pedidos vinculados**, timestamps (`dataCriacao`, `dataFinalizacao`), pesos, distâncias e status.

---

## 📏 Dados Reais Utilizados (Limites & Capacidades)

A simulação é calibrada com base em parâmetros e restrições reais da operação logística por drone:

| Parâmetro | Valor / Limite | Descrição |
| :--- | :--- | :--- |
| **Capacidade de Carga** | `2.5 kg` (máx) | Carga máxima suportada por voo (individual ou soma de pedidos agrupados). |
| **Raio de Alcance** | `8.0 km` (máx) | Raio máximo a partir da base (garante 8 km ida + 8 km volta = 16 km de autonomia). |
| **Autonomia de Distância** | `16.0 km` (máx) | Percurso total máximo suportado por uma bateria completa (100%). |
| **Taxa de Recarga** | `+5%` por ciclo | Ciclo de carregamento gradual do drone no backend. |
| **Limitação de Área** | Grade 2D / PostGIS | Delimitação da zona de voo em Belo Horizonte com suporte a no-fly zones. |

---

## 🗄️ Caracterização do Banco de Dados (PostgreSQL + PostGIS)

Para suportar a auditoria e rastreabilidade, o banco mapeia as seguintes informações essenciais:

┌─────────────────────────┐          ┌─────────────────────────┐          ┌─────────────────────────┐
│          DRONE          │          │         ENTREGA         │          │         PEDIDO          │
├─────────────────────────┤          ├─────────────────────────┤          ├─────────────────────────┤
│ [PK] id                 │          │ [PK] id                 │          │ [PK] id                 │
│      codigo (e.g. DD01) │──(1:N)──>│      codigo_entrega     │──(1:N)──>│      numero_pedido      │
│      bateria            │          │ [FK] drone_id           │          │ [FK] entrega_id         │
│      status             │          │      data_inicio        │          │      peso               │
│      base_origem        │          │      data_fim           │          │      distancia          │
└─────────────────────────┘          └─────────────────────────┘          │      prioridade         │
                                                                          │      status             │
                                                                          └─────────────────────────┘

* **Pedidos:** Armazena código do pedido (`#PED-xxx`), peso (kg), distância (km), prioridade, coordenadas, status (`NA_FILA`, `EM_VOO`, `ENTREGUE`) e timestamps.
* **Entregas:** Registra o evento da viagem, vinculando o drone executor, o horário de saída/conclusão e a lista de pedidos atendidos no mesmo lote.
* **Drones:** Mantém o nível de bateria atual, status operacional, localização em tempo real e histórico de viagens.

---

## 🛠️ Stack Tecnológica & Requisitos

### 🔹 Backend
* **Java JDK:** 21 (Eclipse Temurin)
* **Framework:** Spring Boot 3.x (*Web, Data JPA, Validation, SSE*)
* **Build Tool:** Maven 3.9+

### 🔹 Frontend
* **Node.js:** 20.x
* **Bundler:** Vite 5.x
* **Linguagem:** TypeScript 5.x
* **Biblioteca:** React 18

### 🔹 Infraestrutura
* **Database:** PostgreSQL 16 com extensão PostGIS (Porta `5432`)
* **Containerização:** Docker Engine & Docker Compose (v24+)

---

## 🚀 Guia Rápido de Inicialização

### 1. Clonando o Repositório

```bash
git clone [https://github.com/byluxp/teste-pratico-DTI.git](https://github.com/byluxp/teste-pratico-DTI.git)
cd teste-pratico-DTI
```
### 2. Formas de Execução
Escolha uma das abordagens abaixo para subir a aplicação:

🟢 Método Recomendado (Via Docker)
Sobe toda a infraestrutura (Banco, API e Frontend servido via NGINX) com um único comando:

```bash 
docker-compose up --build
```

## 🟡 Método Local (Modo Desenvolvimento)
Ideal para debug e alterações em tempo de execução:

### 1. Inicie o banco de dados:

```bash 
docker-compose up postgres -d
```
### 2. Inicie a API (Spring Boot):

```bash 
cd spring-app
# Certifique-se de configurar o arquivo .env se necessário
./mvnw spring-boot:run
```
### 3. Inicie a interface React:

```bash 
cd ../FrontEnd
npm install
npm run dev
```

Frontend Dev: http://localhost:5173

## 🧪 Qualidade de Código & Testes
O projeto conta com suítes de testes unitários e de integração cobrindo capacidade de carga dos drones, consumo de bateria por prioridade, agrupamento de pedidos e regras de negócio.

Diretório dos Testes: ./spring-app/src/test/java/com/example/demo

Para executar a suíte de testes via terminal:
```bash 
cd spring-app
./mvnw test
```
🤖 Desenvolvimento Auxiliado por IA
Todo o processo de arquitetura, estruturação e tomadas de decisão contou com auxílio de Inteligência Artificial. Os registros de prompts, restrições e logs de pensamento (Chain-of-Thought) estão documentados transparentemente.

📄 Acessar histórico de prompts e logs (AI_LOGS.md)