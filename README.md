Searchoteca - Web Frontend
Frontend web do sistema Searchoteca, responsável por controlar o estoque de vitrine (estantes) de uma livraria — livros, departamentos e localizações — através de uma interface visual construída com Spring Boot + Thymeleaf.
Este módulo não implementa regras de negócio nem persistência: ele consome a API REST do backend do Searchoteca e apresenta os dados em páginas HTML, permitindo cadastrar, listar, editar e excluir os registros do acervo.
Índice
Sobre o projeto
Arquitetura
Tecnologias
Pré-requisitos
Configuração
Como executar
Funcionalidades
Estrutura do projeto
Endpoints da aplicação
Sobre o projeto
O Searchoteca é dividido em módulos independentes; este repositório contém apenas a camada web/frontend. A aplicação:
Renderiza páginas server-side com Thymeleaf (sem framework JS de SPA);
Delega toda a leitura e escrita de dados para uma API backend (via `RestClient`);
Organiza o acervo em três entidades principais: Livros, Departamentos e Localizações.
Arquitetura
```
Navegador  <-->  Searchoteca Web Frontend (Spring MVC + Thymeleaf, porta 8081)  <-->  Searchoteca Backend (API REST, porta 8080)
```
O frontend atua como um cliente HTTP do backend: cada operação da interface (listar, criar, editar, excluir) dispara uma chamada REST correspondente ao serviço `BackendService`, usando um `RestClient` configurado com a URL base do backend.
Tecnologias
Java 21
Spring Boot 4.1.1
Spring Web / Spring MVC
Spring RestClient (consumo da API do backend)
Thymeleaf (templates HTML server-side)
Lombok
Maven (com Maven Wrapper incluso)
Pré-requisitos
JDK 21 instalado
Maven (ou use o `mvnw`/`mvnw.cmd` incluso no projeto)
O backend do Searchoteca rodando e acessível (por padrão em `http://localhost:8080`), já que este frontend depende dele para todas as operações de dados
Configuração
As configurações da aplicação ficam em `Searchoteca Web/frontend/src/main/resources/application.yaml`:
```yaml
server:
  port: 8081

spring:
  application:
    name: frontend

# URL onde o backend (projeto Searchoteca) esta rodando
backend:
  base-url: http://localhost:8080
```
`server.port`: porta em que o frontend será exposto (padrão `8081`).
`backend.base-url`: URL base da API do backend do Searchoteca. Ajuste esse valor caso o backend esteja rodando em outro host/porta.
Como executar
Clone o repositório e acesse a pasta do projeto:
```bash
   git clone https://github.com/GraziPO/Searchoteca-WebFrontend.git
   cd "Searchoteca-WebFrontend/Searchoteca Web/frontend"
   ```
Garanta que o backend do Searchoteca esteja em execução (ou ajuste `backend.base-url` no `application.yaml` para apontar para onde ele está rodando).
Execute a aplicação com o Maven Wrapper:
```bash
   ./mvnw spring-boot:run
   ```
No Windows:
```bash
   mvnw.cmd spring-boot:run
   ```
Acesse no navegador:
```
   http://localhost:8081
   ```
Funcionalidades
A aplicação organiza o gerenciamento do acervo em três áreas, cada uma com listagem, cadastro, edição e exclusão:
Livros — cadastro com ISBN, título, autor, ano de lançamento, editora, gênero, departamento e localização.
Departamentos — código, nome e descrição do departamento.
Localizações — código, nome, descrição e departamento associado.
Fluxo típico de cada entidade:
Listagem dos registros (`GET`)
Formulário de novo registro (`GET` + `POST`)
Formulário de edição de registro existente (`GET` + `POST`)
Exclusão de registro (`POST`)
Estrutura do projeto
```
Searchoteca Web/frontend
├── src/main/java/searchoteca/frontend
│   ├── FrontendApplication.java        # classe principal (entry point)
│   ├── config/RestClientConfig.java    # configuração do RestClient para o backend
│   ├── controller/PageController.java  # rotas e páginas (livros, departamentos, localizações)
│   ├── model/                          # Book, Department, Location
│   └── service/BackendService.java     # chamadas HTTP para a API do backend
├── src/main/resources
│   ├── application.yaml                # configuração (porta, URL do backend)
│   ├── static/css/style.css            # estilos da aplicação
│   └── templates/                      # páginas Thymeleaf (index, acervo, livros, departamentos, localizações)
└── pom.xml
```
Endpoints da aplicação
Método	Rota	Descrição
GET	`/`	Página inicial
GET	`/acervo`	Menu do acervo
GET	`/acervo/livros`	Lista de livros
GET	`/acervo/livros/novo`	Formulário de novo livro
POST	`/acervo/livros`	Cria um livro
GET	`/acervo/livros/{isbn}/editar`	Formulário de edição de livro
POST	`/acervo/livros/{isbn}/editar`	Atualiza um livro
POST	`/acervo/livros/{isbn}/excluir`	Exclui um livro
GET	`/acervo/departamentos`	Lista de departamentos
GET	`/acervo/departamentos/novo`	Formulário de novo departamento
POST	`/acervo/departamentos`	Cria um departamento
GET	`/acervo/departamentos/{departCode}/editar`	Formulário de edição de departamento
POST	`/acervo/departamentos/{departCode}/editar`	Atualiza um departamento
POST	`/acervo/departamentos/{departCode}/excluir`	Exclui um departamento
GET	`/acervo/localizacoes`	Lista de localizações
GET	`/acervo/localizacoes/novo`	Formulário de nova localização
POST	`/acervo/localizacoes`	Cria uma localização
GET	`/acervo/localizacoes/{localCode}/editar`	Formulário de edição de localização
POST	`/acervo/localizacoes/{localCode}/editar`	Atualiza uma localização
POST	`/acervo/localizacoes/{localCode}/excluir`	Exclui uma localização
Todas essas rotas fazem, internamente, chamadas correspondentes à API REST do backend (`/api/livro`, `/api/departamento`, `/api/localizacao`).
