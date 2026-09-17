# Biblioteca Online — Backend

API REST para cadastro e listagem de usuários, autenticação por CPF/senha e consulta à Google Books. O cadastro completa o endereço consultando ViaCEP. Veja o [guia da raiz](../README.md) para clonar, preparar os `.env` e iniciar todos os serviços.

## Tecnologias e estrutura

Spring Boot 4.1.1, Java com alvo 21, Spring MVC, Spring Data JPA, PostgreSQL, Spring Security, JWT HS256, Bean Validation, MapStruct e Lombok. O projeto inclui Springdoc/OpenAPI. O Maven Wrapper está configurado para Maven 3.9.16.

Em `src/main/java/com/biblioteca/Biblioteca/Online`:

- `autenticacao`: login, geração de tokens e regras de segurança.
- `usuario`: DTOs, entidades, mapeamento, persistência, cadastro e consulta de endereços.
- `biblioteca`: controllers, serviço e cliente da Google Books.
- `shared`: tratamento de erros comuns.
- `config`: configuração dos clientes HTTP.

As propriedades ficam em `src/main/resources/application.properties`. O esquema do banco é atualizado pelo Hibernate com `spring.jpa.hibernate.ddl-auto=update`; não há scripts de migração nesta cópia.

## Executar com Docker

Prepare os arquivos de ambiente seguindo o README da raiz. Na **raiz do projeto**, execute:

```sh
docker compose up -d --build backend
docker compose logs -f backend
```

O Compose também inicia o PostgreSQL e aguarda sua verificação de saúde. A API publica a porta 8080. O Dockerfile compila com Maven/JDK 25, pula testes no build e executa o JAR com JRE 25. `Dockerfile.dev` existe, mas não é usado pelo Compose ativo.

## Executar localmente

Instale um JDK compatível com o alvo Java 21 e configure `JAVA_HOME`. Para reproduzir a versão de execução do Docker, use JDK 25. Não é necessário instalar Maven separadamente: use o wrapper incluído. A primeira execução precisa de internet para baixar Maven e dependências.

1. Inicie o banco com `docker compose up -d postgres` na raiz, após configurar os três `.env` exigidos pelo Compose.
2. Nesta pasta, copie `.env.example` para `.env` se ainda não existir e preencha os valores; se já existir, ajuste-o. O modelo tem apenas variáveis vazias. Para execução local, use como referência:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/biblioteca
SPRING_DATASOURCE_USERNAME=biblioteca
SPRING_DATASOURCE_PASSWORD=troque_esta_senha_local
GOOGLE_BOOKS_API_BASE_URL=https://www.googleapis.com
GOOGLE_BOOKS_API_KEY=
JWT_SECRET=substitua-por-um-segredo-aleatorio-com-32-ou-mais-bytes
JWT_ISSUER=biblioteca-online
JWT_EXPIRES_IN_MINUTES=60
```

Use as credenciais reais de `Biblioteca-Online-BD/.env`. O backend possui a dependência `springboot4-dotenv` para leitura do `.env`; execute a partir desta pasta. Variáveis já definidas no ambiente do processo também devem ser verificadas se a configuração esperada não for aplicada. Ao voltar ao Compose, altere o host da URL para `postgres`.

3. No Windows/PowerShell:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

Em Linux/macOS:

```sh
sh ./mvnw spring-boot:run
```

## Configuração

| Variável | Finalidade e padrão |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC obrigatória; host `postgres` no Compose e `localhost` localmente. |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco; obrigatório. |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco; obrigatória. |
| `GOOGLE_BOOKS_API_BASE_URL` | Padrão `https://www.googleapis.com`. |
| `GOOGLE_BOOKS_API_KEY` | Chave própria do Google Cloud, com Books API ativada no mesmo projeto; configure para consultar o catálogo. |
| `JWT_SECRET` | Segredo HS256 de pelo menos 32 bytes; configure um valor próprio. |
| `JWT_ISSUER` | Padrão `biblioteca-online`. |
| `JWT_EXPIRES_IN_MINUTES` | Padrão `60`. |

`POSTGRES_*` é usado na inicialização do contêiner do banco, não substitui as propriedades `SPRING_DATASOURCE_*`. A URL do ViaCEP está definida no código como `https://viacep.com.br/ws/{cep}/json/`.

### Chave da Google Books

Siga o [passo a passo de configuração do Google Cloud e ativação da Books API](../README.md#configurar-o-google-cloud-e-ativar-a-books-api): crie/selecione um projeto, ative **Books API** na biblioteca de APIs, gere uma chave em **APIs e serviços → Credenciais** e restrinja seu uso à **Books API**.

Salve a chave somente em `GOOGLE_BOOKS_API_KEY` no `.env` desta pasta. Os modelos `.env.example` devem continuar vazios. Após alterar o arquivo, recrie o contêiner com `docker compose up -d --force-recreate backend` na raiz, ou reinicie o processo local. Embora o código aceite chave vazia para iniciar, as consultas públicas devem identificar a aplicação conforme a [documentação oficial da Books API](https://developers.google.com/books/docs/v1/using#APIKey).

## Endpoints

| Método | Caminho | Acesso | Finalidade |
| --- | --- | --- | --- |
| POST | `/api/usuarios` | Público | Cadastrar usuário. |
| POST | `/api/auth/login` | Público | Autenticar e obter token. |
| GET | `/api/usuarios` | JWT | Listar usuários. |
| GET | `/api/livros?termo=java&page=0&size=10` | JWT | Buscar livros com filtro `free-ebooks`. |
| GET | `/api/livros/{volumeId}` | JWT | Consultar detalhes de um volume. |

A paginação começa em zero, tem tamanho padrão 10 e limite configurado de 40. Não há autorização por perfil: qualquer usuário autenticado pode acessar a listagem de usuários.

Exemplo de corpo JSON para cadastro:

```json
{
  "nome": "Pessoa de teste",
  "cpf": "12345678901",
  "senha": "senha-local-123",
  "endereco": {
    "cep": "01001000",
    "numero": "42",
    "complemento": ""
  }
}
```

Os dados acima são fictícios para ambiente local. Na API, CPF deve ter 11 dígitos sem máscara e CEP deve ter 8; nome, senha, endereço e número são obrigatórios. A senha exige pelo menos 8 caracteres. CPF duplicado é rejeitado e o CEP precisa ser encontrado pelo ViaCEP.

Corpo de login:

```json
{"cpf":"12345678901","senha":"senha-local-123"}
```

A resposta inclui `accessToken`, `tokenType` e `expiresIn` em segundos. Envie `Authorization: Bearer TOKEN` nas chamadas privadas. As senhas são armazenadas com BCrypt.

Abra http://localhost:8080/swagger-ui/index.html para explorar a API. A especificação está em http://localhost:8080/v3/api-docs. Esses caminhos são públicos e devem ser acessados diretamente pela porta 8080, pois o Nginx atual só encaminha `/api` ao backend.

## Compilar e verificar

Nesta pasta, no PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target/Biblioteca-Online-0.0.1-SNAPSHOT.jar
```

Em Linux/macOS, substitua `.\mvnw.cmd` por `sh ./mvnw`. Não foram encontrados testes em `src/test` nesta cópia; `test` não comprova cobertura funcional. Para verificar a integração, cadastre um usuário, faça login e consulte usuários/livros com o token. Essas operações dependem do banco e dos serviços externos. O build e esse fluxo não foram executados nesta revisão documental.
