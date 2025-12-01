# 🗂 PROJECT MANAGEMENT API

Solução de API RESTful, inspirada em ferramentas como Trello, desenvolvida com **Spring Boot 3** para gerenciar o ciclo de vida de projetos e tarefas. O projeto prioriza a **segurança** (via Spring Security e JWT) e a **comunicação em tempo real** (WebSocket e SSE) para notificações e mensagens.

---

## 🏗️ Engenharia de DevOps e Qualidade

Este projeto demonstra proficiência em práticas de **Desenvolvimento e Operações (DevOps)**, focando em portabilidade, segurança e eficiência no ambiente de desenvolvimento e produção.

- **Containerização Plug-and-Play:** Uso de **Docker** e **Docker Compose** para orquestrar o ambiente multi-serviço (API + DB PostgreSQL) com um único comando.
- **Imagens Seguras e Otimizadas:** A imagem da API é construída usando **`Dockerfile` limpo e seguro** (com usuário não-root) e otimização de tamanho via **Multi-Stage Build**.
- **Segurança de Credenciais Prioritária:**
  - **Separação de Segredos:** Credenciais sensíveis (chaves JWT, senhas do DB) são injetadas **exclusivamente via Variáveis de Ambiente** (`.env`).
  - **Controle de Versão:** Arquivos de configuração de ambiente (`application-dev.properties`, `.env`) são ativamente **ignorados** pelo Git, prevenindo a exposição acidental de dados no repositório público.
- **Orquestração Robusta:** O **`docker-compose.yml`** utiliza mecanismos como **`healthcheck`** e **`depends_on`** para garantir que os serviços (como o DB) estejam prontos e íntegros antes da inicialização da API.

---

## 🚀 Tecnologias Essenciais

| Categoria            | Tecnologias Utilizadas                                            |
| :------------------- | :---------------------------------------------------------------- |
| **Backend Core**     | **Java 21**, **Spring Boot 3**, Maven                             |
| **Segurança**        | **Spring Security** (Autenticação), **JWT** (JSON Web Tokens)     |
| **Persistência**     | **Spring Data JPA**, **PostgreSQL**                               |
| **Comunicação RT**   | **WebSocket** (Mensagens), **SSE** (Notificações em tempo real)   |
| **Deploy/Qualidade** | **Docker**, **Docker Compose**, JUnit 5, Mockito, Swagger/OpenAPI |

---

## 🔑 Funcionalidades Chave

- **Usuários:** Cadastro, login, atualização, gerenciamento de roles (**ADMIN/CLIENTE**).
- **Projetos:** Criação, gerenciamento de status e associação dinâmica de usuários.
- **Tarefas:** Criação, atualização de status e atribuição a usuários.
- **Mensagens:** Envio, leitura e exclusão em tempo real via **WebSocket**.
- **Notificações:** Envio e histórico em tempo real via **SSE (Server-Sent Events)**.
- **Segurança:** Endpoints protegidos por **roles** e fluxo de autenticação e autorização completo.

---

## 🐳 Como Rodar a Aplicação (Setup Plug-and-Play)

O ambiente de desenvolvimento completo (API + DB PostgreSQL) é inicializado através do Docker Compose.

### Pré-requisitos

1.  Ter o **Docker** e o **Docker Compose** instalados.
2.  Criar o arquivo **`.env`** na raiz do projeto para carregar as credenciais (este arquivo **NÃO** deve ser commitado):

    ```env
    DB_USERNAME=
    DB_PASSWORD=
    JWT_SECRET_KEY=
    ```

### Execução

Na raiz do projeto, execute:

```bash
docker-compose up --build

## 📂 Estrutura de Pacotes

com.matheusluizroza.project_management_api
┣ 📂 config → Configurações de segurança e Swagger
┣ 📂 controller → Endpoints REST e WebSocket
┣ 📂 dto → Objetos de transferência de dados
┣ 📂 enums → Enumerações de status e roles
┣ 📂 filter → Filtros JWT
┣ 📂 model → Entidades JPA
┣ 📂 repository → Repositórios Spring Data JPA
┣ 📂 service → Lógica de negócio
┗ ProjectManagementApiApplication.java


## 📖 Documentação Swagger

Após rodar a aplicação, acesse:
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🧪 Testes

Testes unitários e de integração utilizando:

- **JUnit 5**
- **Mockito**

---

## 👨‍💻 Autor

**Matheus Luiz (Math-l7)**
```
