# Logs e Prompts de Desenvolvimento da IA (Antigravity)

Este documento registra as principais instruções, regras arquiteturais e trocas de prompts com a Inteligência Artificial durante a construção do **Simulador de Entregas por Drone**.

---

## 🧠 Memória Arquitetural e Regras de Negócio

Durante o processo, a IA precisou trabalhar sob as seguintes regras do domínio:

1. **Capacidade do Drone:** Um drone suporta um peso máximo X (kg) e tem autonomia Y (km) por carga. A alocação foi definida para priorizar pedidos com base na distância e preenchimento de mochila (Knapsack problem adaptado de forma Gulosa).
2. **Mapeamento (Grid 2D):** A cidade é visualizada numa malha bidimensional. O Frontend usa porcentagens relativas (0% a 100%) enquanto o Backend calcula distâncias Euclidianas.
3. **Obstáculos (Zonas de Exclusão):** O usuário pode selecionar zonas (quadradas visualmente, tratadas via raio seguro no back) onde o drone **não pode entrar**.
4. **Collision Evasion:** A IA codificou um sistema dinâmico no `MapGrid.tsx` onde a animação do drone desvia ativamente (cria waypoints vetoriais) caso o caminho original intercepte o raio de um obstáculo.
5. **Máquina de Estados:** O fluxo lógico do drone passa por: `IDLE` → `CARREGANDO` → `EM_VOO` (ou EM_TRANSITO) → `ENTREGANDO` → `RETORNANDO` → `IDLE`.

---

## 📝 Histórico de Prompts Utilizados

Aqui estão os principais prompts estruturais enviados pelo desenvolvedor (User) que guiaram as decisões de código:

### 1. Modelagem Inicial e Regras de Negócio

> **User:** "atenaçao revise se estes requisito a seguir estao implementados: Simulador de Encomendas em Drone. Regras Básicas: Capacidade: Cada drone suporta até X kg e pode viajar até Y km por carga. Mapeamento: A cidade é uma malha de coordenadas (exemplo: 2D). Sistema de Pedidos: O sistema deve receber pedidos com Localização do cliente (X, Y), Peso do pacote, Prioridade da entrega. O sistema precisa alocar os pacotes nos drones com o menor número de viagens possível, respeitando as regras. Simular bateria, Inserir obstáculos, Calcular tempo total, Criar uma fila de entrega."

### 2. Visão Gráfica 2D

> **User:** "agora vamos criar a representaçao do front end simples, simulando visualmente em 2d o drone indo do ponto a ao b, e um icone de bateria e um icone de dash board, e a tendo a funcionalidade de marcar uma area como o selecionar do windows para os obstaculos, e tambem o icone da fila de entregas"

### 3. Integração Fullstack de Pedidos

> **User:** "no back end crie a opçao de criar pedidos com seus respectivos campos,, e implemento isso no front end"

### 4. Navegação Inteligente (Evasion)

> **User:** "revise para que o drone de a volta no obstaculo"

### 5. DevOps e Pipeline

> **User:** "revise os docker file"
> **User:** "revise o gitwrok flow"

### 6. Dashboard Estatístico

> **User:** "crie a funcionalidade de dashboard. Criar uma visualização simples com: Quantidade de entregas realizadas, Tempo médio por entrega, Drone mais eficiente, Mapa das entregas."

---

## 🛠️ Controles e Constraints Utilizados pela IA

Além das requisições diretas, a IA operou respeitando as seguintes restrições:

- **Separação de Camadas:** Manutenção estrita da separação Controller / Service / Model do Spring Boot.
- **CSS Avançado e UX:** Implementação de `glass-panels`, variáveis de cores Neón (`--cyan`, `--red`) e matrizes CSS Transforms `(scale, translate)` de alta performance via GPU para pan e zoom fluídos.
