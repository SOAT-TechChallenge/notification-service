# Notification Service

Um serviço de notificação desenvolvido em Spring Boot para envio de emails via SMTP do Gmail. O projeto segue uma arquitetura limpa (Clean Architecture) e inclui testes automatizados.

## Funcionalidades

- Envio de emails através de API REST
- Validação de dados de entrada
- Documentação automática da API com Swagger/OpenAPI
- Configuração de infraestrutura como código com Terraform
- Containerização com Docker
- Testes automatizados com cobertura de código (Jacoco)

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.8-SNAPSHOT**
- **Spring Web** - Para criação da API REST
- **Spring Mail** - Para envio de emails
- **Spring Validation** - Para validação de dados
- **SpringDoc OpenAPI** - Para documentação da API
- **Lombok** - Para redução de código boilerplate
- **Jacoco** - Para cobertura de testes
- **Docker** - Para containerização
- **Terraform** - Para infraestrutura como código
- **Maven** - Para gerenciamento de dependências e build

## Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Docker (opcional, para execução em container)
- Conta Gmail com autenticação de dois fatores habilitada (para gerar senha de app)

## Instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/notification-service.git
cd notification-service
```

2. Instale as dependências:
```bash
mvn clean install
```

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente para o envio de emails:

- `EMAIL_USER`: Seu endereço de email do Gmail
- `EMAIL_PASS`: Senha de app gerada no Gmail (não a senha da conta)
- `EMAIL_FROM`: Endereço de email que aparecerá como remetente

### Arquivo .env (para Docker Compose)

Para execução com Docker Compose, crie um arquivo `.env` na raiz do projeto com as variáveis de ambiente:

```env
EMAIL_USER=seu-email@gmail.com
EMAIL_PASS=sua-senha-app
EMAIL_FROM=seu-email@gmail.com
```

### Arquivo application.properties

O arquivo `src/main/resources/application.properties` já está configurado para usar o SMTP do Gmail:

```properties
spring.application.name=notification_service

# ===== CONFIG MAIL SMTP GMAIL =====
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USER}
spring.mail.password=${EMAIL_PASS}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# E-mail que vai aparecer como remetente
app.mail.from=${EMAIL_FROM}
```

## Execução

### Desenvolvimento

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

### Com Docker

1. Construa a imagem:
```bash
docker build -t notification-service .
```

2. Execute o container:
```bash
docker run -p 8083:8080 \
  -e EMAIL_USER=seu-email@gmail.com \
  -e EMAIL_PASS=sua-senha-app \
  -e EMAIL_FROM=seu-email@gmail.com \
  notification-service
```

A aplicação estará disponível em `http://localhost:8083`

### Com Docker Compose

1. Configure o arquivo `.env` com suas credenciais de email.

2. Execute a aplicação:
```bash
docker-compose up
```

A aplicação estará disponível em `http://localhost:8083`

## Documentação da API

A documentação da API está disponível via Swagger UI em:
- `http://localhost:8080/swagger-ui.html` (desenvolvimento)
- `http://localhost:8083/swagger-ui.html` (Docker)

### Endpoint Disponível

#### POST /api/notification/send-email

Envia um email para o destinatário informado.

**Request Body:**
```json
{
  "to": "destinatario@email.com",
  "subject": "Assunto do email",
  "body": "Corpo do email"
}
```

**Resposta:** 200 OK (sem corpo)

## Testes

### Executar Todos os Testes

```bash
mvn test
```



### Cobertura de Testes

Para gerar relatório de cobertura:

```bash
mvn jacoco:report
```

O relatório estará disponível em `target/site/jacoco/index.html`

## Deploy

### Infraestrutura

A infraestrutura é gerenciada via Terraform. Os arquivos estão em `infra/`:

- `main.tf`: Definição da infraestrutura
- `terraform.tfvars.example`: Exemplo de variáveis do Terraform

### CI/CD

O projeto inclui um workflow do GitHub Actions para CI/CD localizado em `.github/workflows/terraform.yml`

## Estrutura do Projeto

```
.
├── .env (arquivo de variáveis de ambiente)
├── docker-compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── .github/
│   └── workflows/
│       └── terraform.yml
├── infra/
│   ├── data.tf
│   ├── main.tf
│   ├── outputs.tf
│   ├── providers.tf
│   ├── terraform.tfvars
│   └── variables.tf
└── src/
    ├── main/
    │   ├── java/com/fiap/notification_service/
    │   │   ├── NotificationServiceApplication.java
    │   │   ├── _webApi/
    │   │   │   ├── controller/
    │   │   │   │   ├── NotificationWebController.java
    │   │   │   │   └── errorHandler/
    │   │   │   └── dto/
    │   │   │       └── SendEmailRequestDTO.java
    │   │   └── core/
    │   │       ├── application/
    │   │       │   └── useCases/
    │   │       │       └── notification/
    │   │       │           └── SendEmailUseCase.java
    │   │       ├── controller/
    │   │       │   └── NotificationController.java
    │   │       └── gateways/
    │   │           └── notification/
    │   │               ├── EmailNotificationGateway.java
    │   │               └── EmailNotificationGatewayImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/fiap/notification_service/
            ├── NotificationServiceApplicationTests.java
            ├── _webApi/controller/errorHandler/
            │   └── GlobalHandlerExceptionTest.java
            ├── core/application/useCases/notification/
            │   └── SendEmailUseCaseTest.java
            ├── core/controller/
            │   └── NotificationControllerTest.java
            └── core/gateways/notification/
                └── EmailNotificationGatewayImplTest.java
```

## Arquitetura

O projeto segue os princípios da Clean Architecture:

- **_webApi**: Camada de apresentação (controllers, DTOs)
- **core**: Regras de negócio
  - **application**: Casos de uso
  - **controller**: Controladores da aplicação
  - **gateways**: Interfaces para infraestrutura externa

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença [MIT](LICENSE).
