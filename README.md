# 🚁 Simulador de Entregas por Drone
 
> **Desenvolvido por:** Lucila Cardoso

Uma solução logística full-stack projetada para automação de entregas com drones. O sistema gerencia alocação de pedidos e calcula rotas otimizadas em um mapa 2D, desviando dinamicamente de zonas de exclusão aérea.

---

## 📌 Visão Geral do Projeto

| Camada | Tecnologia Principal | Descrição |
| :--- | :--- | :--- |
| **Frontend** | React 18 + Vite | Interface do usuário e renderização do mapa |
| **Backend** | Java 21 + Spring Boot | Regras de negócio, rotas e alocação de pedidos |
| **Banco de Dados** | PostgreSQL 16 + PostGIS | Geoprocessamento e persistência relacional |
| **Containers** | Docker & Docker Compose | Padronização e orquestração do ambiente |

---

## 🛠️ Stack Tecnológica & Requisitos

### 🔹 Backend
* **Java JDK:** 21 (Eclipse Temurin)
* **Framework:** Spring Boot 3.x (*Web, Data JPA, Validation*)
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

## 🚀 Guia Rápidas de Inicialização

### 1. Clonando o Repositório

```bash
git clone [https://github.com/byluxp/teste-pratico-DTI.git](https://github.com/byluxp/teste-pratico-DTI.git)

cd teste-pratico-DTI
```

### 2. Formas de Execução
Escolha uma das abordagens abaixo para subir a aplicação:

🟢 Método Recomendado (Via Docker)
Sobe toda a infraestrutura (Banco, API e Frontend servido via NGINX) com um único comando.

``` bash 
docker-compose up --build
```
* Frontend: http://localhost:3000

* API Backend: Porta :8080

Após rodar o programa, finalize o docker. 

```bash
docker-compose down
```

### 🟡 Método Local (Modo Desenvolvimento)
Ideal para debug e alterações em tempo de execução.

* 1. Inicie o banco de dados:

```bash
docker-compose up postgres -d
``` 
* 2. Inicie a API (Spring Boot):

```bash 
cd spring-app
# Certifique-se de configurar o arquivo .env
./mvnw spring-boot:run
```

* 3. Inicie a interface react: 

```bash
cd ../FrontEnd
npm install
npm run dev
```
* Frontend Dev: http://localhost:5173

## 🧪 Qualidade de Código & Testes

O projeto conta com suítes de testes unitários automatizados cobrindo capacidade de carga dos drones, alocação de pedidos e regras de negócio.

Diretório dos Testes: ./spring-app/src/test/java/com/example/demo

Para executar a suíte de testes via terminal:

```bash 
cd spring-app
./mvnw test
```
## 🤖 Desenvolvimento Auxiliado por IA
Todo o processo de arquitetura, estruturação e tomadas de decisão contou com auxílio de Inteligência Artificial. Os registros de prompts, restrições e logs de pensamento (Chain-of-Thought) estão documentados transparentemente.

📄 Acessar histórico de prompts e logs (AI_LOGS.md)
