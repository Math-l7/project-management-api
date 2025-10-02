🗂 Project Management API

API RESTful desenvolvida em Spring Boot para gerenciar usuários, projetos, tarefas, mensagens e notificações em tempo real, com autenticação via JWT e controle de acesso com Spring Security. Inspirada em ferramentas de gerenciamento de projetos como Trello.

🚀 Tecnologias

Java 21

Spring Boot 3

Spring Security + JWT

Spring Data JPA

PostgreSQL

Maven

WebSocket + SSE (mensagens e notificações em tempo real)

JUnit 5 + Mockito (testes unitários)

Swagger/OpenAPI

🔑 Funcionalidades

Usuários: cadastro, login, atualização, alteração de senha e gerenciamento de roles (ADMIN/CLIENTE).

Projetos: criação, atualização, exclusão, gerenciamento de status e associação de usuários.

Tarefas: criação, atualização, exclusão, alteração de status e atribuição a usuários.

Mensagens: envio, leitura e exclusão em tempo real via WebSocket.

Notificações: envio e leitura em tempo real via SSE, com histórico por usuário.

Segurança: login com JWT, endpoints protegidos por roles, autenticação e autorização.

📂 Estrutura de Pacotes
com.matheusluizroza.project_management_api
┣ 📂 config        → Configurações de segurança e Swagger
┣ 📂 controller    → Endpoints REST e WebSocket
┣ 📂 dto           → Objetos de transferência de dados
┣ 📂 enums         → Enumerações de status e roles
┣ 📂 filter        → Filtros JWT
┣ 📂 model         → Entidades JPA
┣ 📂 repository    → Repositórios Spring Data JPA
┣ 📂 service       → Lógica de negócio
┗ ProjectManagementApiApplication.java

📖 Documentação Swagger

Após rodar a aplicação, acesse:
👉 http://localhost:8080/swagger-ui.html

🧪 Testes

Testes unitários e de integração utilizando:

JUnit 5

Mockito

👨‍💻 Autor

Matheus Luiz (Math-l7) 
