# 🗂 PROJECT MANAGEMENT API

API RESTful desenvolvida em **Spring Boot** para gerenciar **usuários, projetos, tarefas, mensagens e notificações em tempo real**, com autenticação via **JWT** e controle de acesso com **Spring Security**. Inspirada em ferramentas de gerenciamento de projetos como **Trello**.

---

## 🏗️ Arquitetura e Estratégia de Deploy

Este projeto é totalmente **containerizado**, demonstrando proficiência em **Docker** e **Docker Compose** para orquestração de ambientes multi-serviço (API + DB).

* **Containerização Segura:** A imagem da API foi criada com um **`Dockerfile` limpo e seguro**, seguindo as melhores práticas (uso de usuário **não-root** e ausência de segredos *hardcoded*).
* **Orquestração Plug-and-Play:** O **`docker-compose.yml`** configura o ambiente completo, subindo a **API Spring Boot** e o **Banco de Dados PostgreSQL** com um único comando, utilizando **`healthcheck`** e **`depends_on`** para garantir a ordem e a disponibilidade dos serviços.
* **Segurança de Segredos:** Todas as credenciais (chave JWT, senha do DB) são injetadas via **Variáveis de Ambiente**, garantindo que nenhum dado sensível esteja exposto no código ou na imagem Docker.

---

## 🚀 Tecnologias

- **Java 21**  
- **Spring Boot 3**  
- **Spring Security + JWT**  
- **Spring Data JPA**  
- **PostgreSQL**  
- **Maven**  
- **Docker** e **Docker Compose** (Orquestração e Deploy)
- **WebSocket + SSE** (mensagens e notificações em tempo real)  
- **JUnit 5 + Mockito** (testes unitários)  
- **Swagger/OpenAPI**

---

## 🔑 Funcionalidades

### Usuários
- Cadastro, login e atualização  
- Alteração de senha  
- Gerenciamento de roles (**ADMIN/CLIENTE**)  

### Projetos
- Criação, atualização e exclusão  
- Gerenciamento de status  
- Associação de usuários  

### Tarefas
- Criação, atualização e exclusão  
- Alteração de status  
- Atribuição a usuários  

### Mensagens
- Envio, leitura e exclusão em tempo real via **WebSocket**  

### Notificações
- Envio e leitura em tempo real via **SSE**  
- Histórico por usuário  

### Segurança
- Login com **JWT**  
- Endpoints protegidos por **roles**  
- Autenticação e autorização

---

## 🐳 Como Rodar a Aplicação (Setup Plug-and-Play)

Graças à containerização, o ambiente de desenvolvimento completo (API + DB) pode ser inicializado com um único comando.

### Pré-requisitos
1.  Ter o **Docker** e o **Docker Compose** instalados.
2.  Criar o arquivo **`.env`** na raiz do projeto com os valores necessários (este arquivo **NÃO** deve ser commitado no Git):

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
