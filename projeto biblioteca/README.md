# Biblioteca Online

Aplicação para cadastro e autenticação de usuários e consulta de livros digitais gratuitos na Google Books. O cadastro consulta o endereço pelo ViaCEP. O catálogo apresenta detalhes e links externos para leitura ou download quando fornecidos pela Google Books; não há implementação de empréstimos ou armazenamento local dos livros no código revisado.

## Organização

| Pasta | Responsabilidade | Documentação |
| --- | --- | --- |
| `Biblioteca-Online-Frontend` | Interface Next.js 16.3.5, React 19.2.8, TypeScript e Tailwind CSS 4 | [README](Biblioteca-Online-Frontend/README.md) |
| `Biblioteca-Online-Backend` | API Java, Spring Boot 4.1.1, JPA, Spring Security e JWT | [README](Biblioteca-Online-Backend/README.md) |
| `Biblioteca-Online-BD` | PostgreSQL 16 e persistência | [README](Biblioteca-Online-BD/README.md) |
| `Biblioteca-Online-Nginx` | Entrada HTTP e encaminhamento das requisições | [README](Biblioteca-Online-Nginx/README.md) |

O `docker-compose.yml` conecta os quatro serviços na rede `app-network`. O navegador acessa o Nginx, que envia as páginas ao frontend e `/api` ao backend. O backend acessa PostgreSQL, ViaCEP e Google Books.

## Requisitos da máquina

- Git, se o projeto for obtido de um repositório.
- Docker com suporte a contêineres Linux e Docker Compose, usando o comando `docker compose`. No Windows, use Docker Desktop com o ambiente Linux/WSL 2 configurado; mantenha o Docker em execução.
- Acesso à internet para baixar imagens e dependências e consultar ViaCEP e Google Books.
- Portas locais `80`, `8080` e `5432` disponíveis. O desenvolvimento local do frontend também usa `3000`.

Para executar tudo pelo Compose, não é necessário instalar Java, Maven, Node.js ou PostgreSQL na máquina. Para desenvolvimento fora dos contêineres, consulte os READMEs das aplicações.

Confira as ferramentas:

```sh
git --version
docker --version
docker compose version
docker info
```

## 1. Obter o projeto

Se estiver publicado no Git, substitua `URL_DO_REPOSITORIO` pelo endereço fornecido pelo responsável:

```sh
git clone URL_DO_REPOSITORIO biblioteca-online
cd biblioteca-online
```

Não foram encontrados metadados `.git` dentro desta cópia, portanto não foi possível identificar uma URL oficial. O exemplo pressupõe um repositório contendo o Compose e as quatro pastas. Se você recebeu um ZIP ou já possui esta pasta, abra o terminal na pasta que contém `docker-compose.yml` e pule a clonagem.

## 2. Configurar o ambiente

O Compose exige os três arquivos `.env` abaixo, mesmo que algum não contenha variáveis. Eles podem não acompanhar o clone porque são ignorados pelo Git. Cada pasta de backend, frontend e banco contém um `.env.example` com valores vazios: copie-o para `.env` na mesma pasta, sem sobrescrever um arquivo existente, e preencha conforme as instruções abaixo. Preserve o nome `.env` sem extensão `.txt`. O Nginx não utiliza variáveis de ambiente próprias e não precisa desse arquivo.

Os modelos não contêm credenciais. Antes de iniciar, preencha as variáveis necessárias, incluindo `JWT_EXPIRES_IN_MINUTES` com um número como `60`. Para usar o padrão de uma propriedade opcional do backend, remova a linha correspondente do `.env` em vez de presumir que um valor vazio ativa o padrão. Gere sua própria `GOOGLE_BOOKS_API_KEY` no [Google Cloud Console](https://console.cloud.google.com/), ative a Books API e salve a chave no `.env` do backend. O passo a passo para criar o projeto, ativar o serviço e gerar a chave está na próxima seção. As variáveis `POSTGRES_*` presentes no modelo do backend devem corresponder às do banco, embora a conexão da API utilize `SPRING_DATASOURCE_*`.

### Configurar o Google Cloud e ativar a Books API

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/) e entre com sua conta Google. Se ainda não tiver uma, use a opção **Criar conta** na tela de login e conclua o cadastro.
2. No seletor de projetos, na barra superior, clique em **Novo projeto**. Informe um nome, como `Biblioteca Online`, selecione a organização/localização se solicitado e clique em **Criar**. Após a criação, selecione esse projeto no topo do console.
3. Abra **APIs e serviços → Biblioteca**, pesquise **Books API** e abra o resultado correspondente. Você também pode acessar a [página da Books API](https://console.cloud.google.com/apis/library/books.googleapis.com), conferindo o projeto selecionado.
4. Clique em **Ativar** e aguarde a conclusão. Em **APIs e serviços → APIs e serviços ativados**, confirme que a **Books API** aparece. Se a página já mostrar **Gerenciar**, o serviço já está ativado nesse projeto.
5. No mesmo projeto, abra **APIs e serviços → Credenciais → Criar credenciais → Chave de API**. Dê um nome à chave, caso solicitado. Em **Restrições de API**, selecione **Restringir chave** e escolha **Books API**. Salve/crie a chave; se essa opção aparecer após a criação, abra a chave para editá-la e salve a restrição.
6. As consultas partem do backend. Em **Restrições de aplicativo**, não selecione sites/referenciadores HTTP para essa chave. Para desenvolvimento local sem IP público fixo, use **Nenhuma**, mantendo a restrição de API para **Books API**. Em um servidor com saída fixa, restrinja pelo IP público de saída do backend; não use o IP interno do contêiner ou `localhost`.
7. Copie a chave gerada e, no arquivo **`Biblioteca-Online-Backend/.env`**, cole o valor após `GOOGLE_BOOKS_API_KEY=`. Mantenha `GOOGLE_BOOKS_API_BASE_URL=https://www.googleapis.com`. Não copie a chave para o frontend, README ou `.env.example`, nem a publique no Git.
8. Se o backend já estiver rodando no Docker, execute na raiz `docker compose up -d --force-recreate backend` para aplicar o `.env` atualizado. Um simples `docker compose restart` não atualiza as variáveis do contêiner. Se estiver executando localmente, encerre e inicie novamente o backend.
9. Entre na aplicação e faça uma busca no catálogo. Se houver erro, confira se a chave pertence ao projeto em que a Books API foi ativada, as restrições configuradas e as quotas exibidas em **APIs e serviços → Books API**. Alterações nas restrições podem levar alguns minutos para surtir efeito.

Os nomes dos menus podem variar com o idioma do console. Consulte também as [instruções oficiais para criação e restrição de chaves](https://docs.cloud.google.com/docs/authentication/api-keys).

Os valores abaixo são exemplos para desenvolvimento. Se os arquivos já existirem, revise-os sem sobrescrever credenciais de um ambiente em uso. Não publique senhas ou chaves no Git.

### `Biblioteca-Online-BD/.env`

```dotenv
POSTGRES_DB=biblioteca
POSTGRES_USER=biblioteca
POSTGRES_PASSWORD=troque_esta_senha_local
```

### `Biblioteca-Online-Backend/.env`

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/biblioteca
SPRING_DATASOURCE_USERNAME=biblioteca
SPRING_DATASOURCE_PASSWORD=troque_esta_senha_local
GOOGLE_BOOKS_API_BASE_URL=https://www.googleapis.com
GOOGLE_BOOKS_API_KEY=
JWT_SECRET=substitua-por-um-segredo-aleatorio-com-32-ou-mais-bytes
JWT_ISSUER=biblioteca-online
JWT_EXPIRES_IN_MINUTES=60
```

Use o mesmo nome de banco, usuário e senha nos dois arquivos. Dentro do Compose, o host do banco é `postgres`, não `localhost`. As variáveis `POSTGRES_*` inicializam o banco; as `SPRING_DATASOURCE_*` configuram a conexão do backend e precisam estar coerentes.

A aplicação consegue iniciar com a chave vazia, mas configure uma chave própria para consultar o catálogo: a documentação da Google Books exige identificar as chamadas de dados públicos com chave de API ou token OAuth. Este projeto utiliza chave de API e não precisa de consentimento OAuth para essas consultas. ViaCEP não exige chave no código atual. [Referência oficial da Books API](https://developers.google.com/books/docs/v1/using#APIKey).

Defina um segredo JWT próprio com pelo menos 32 bytes; uma sequência ASCII de 32 ou mais caracteres atende ao tamanho. A aplicação tem um segredo padrão de desenvolvimento, que não deve ser usado em uma publicação real.

### `Biblioteca-Online-Frontend/.env`

```dotenv
INTERNAL_API_BASE_URL=http://localhost:8080
```

Esse endereço serve ao frontend executado localmente. No Compose, `environment` sobrescreve essa variável com `http://backend:8080`. O navegador usa caminhos relativos `/api`; no acesso pela porta 80, o Nginx encaminha essas chamadas diretamente ao backend. Não coloque credenciais do banco ou segredo JWT no frontend.

## 3. Construir e iniciar

Execute na raiz, onde está o Compose:

```sh
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 backend frontend nginx postgres
```

A primeira construção baixa dependências e pode demorar. O banco possui verificação de saúde; frontend e Nginx aguardam apenas o início dos serviços anteriores, não a disponibilidade completa da API. Aguarde o backend terminar de iniciar antes do primeiro cadastro.

| Acesso | Endereço |
| --- | --- |
| Aplicação pelo Nginx | http://localhost |
| API direta | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Documento OpenAPI | http://localhost:8080/v3/api-docs |
| PostgreSQL para ferramentas locais | `localhost:5432` |

A API não tem uma página inicial em `/api`; use os endpoints documentados. O frontend não publica a porta 3000 no Compose atual. O Nginx declara `biblioteca-online.localhost`, mas o acesso local pela porta 80 permite usar `localhost` sem editar o arquivo hosts. A configuração fornecida usa HTTP.

## 4. Primeiro uso

1. Abra http://localhost/cadastro e cadastre uma conta. Informe nome, CPF com 11 dígitos, senha com pelo menos 8 caracteres, CEP válido com 8 dígitos e número do endereço. A interface aceita máscaras de CPF e CEP; complemento é opcional.
2. O backend consulta o ViaCEP e grava o usuário com senha protegida por BCrypt. Não há usuário padrão ou carga inicial identificada.
3. Entre em http://localhost/login com o CPF e a senha cadastrados.
4. Consulte o catálogo em `/catalogo` e os usuários em `/usuarios`. A sessão usa JWT, com validade padrão de 60 minutos.

Atualmente a API permite cadastro público e exige autenticação para listar usuários e consultar livros. Não há separação de acesso por papéis de administrador no código revisado.

## Operação diária

Comandos executados na raiz:

```sh
docker compose logs -f backend
docker compose stop
docker compose start
docker compose down
docker compose up -d --build
```

`stop` para os contêineres; `start` retoma os existentes; `down` remove contêineres e rede, preservando o volume de dados. `up -d --build` reconstrói as aplicações após mudanças. Não há recarga automática de código no Compose ativo: os volumes de desenvolvimento estão comentados.

Os dados ficam no volume `postgres_data`, cujo nome efetivo recebe o prefixo do projeto Compose. **`docker compose down -v` apaga os volumes e os dados do banco**; não use para uma parada comum. Mudar `POSTGRES_*` não altera automaticamente usuários e senhas de um banco já inicializado.

## Desenvolvimento local

Configure primeiro os três `.env`. Na raiz, inicie apenas o banco:

```sh
docker compose up -d postgres
```

Depois siga o [backend](Biblioteca-Online-Backend/README.md) para executar a API com host do banco `localhost`, e o [frontend](Biblioteca-Online-Frontend/README.md) para executar a interface na porta 3000. Evite executar o backend local e o contêiner backend simultaneamente na porta 8080.

## Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| Arquivo `.env` ausente | Crie os três arquivos nos caminhos exatos indicados acima. |
| Docker indisponível | Abra o Docker e confira `docker info`. |
| Porta já ocupada | Encerre o serviço conflitante ou ajuste a porta publicada no Compose e os endereços de acesso. |
| Backend não conecta ao banco | Confira credenciais, logs do PostgreSQL e host `postgres` no Docker ou `localhost` fora dele. |
| Senha do banco alterada sem efeito | As variáveis de inicialização só se aplicam a um diretório de dados vazio; preserve os dados e ajuste o usuário no banco existente. |
| Erro 502 ao abrir o site | Consulte os logs e aguarde a inicialização; confirme que backend e frontend continuam em execução. |
| Cadastro falha | Confira CPF já cadastrado, formato dos campos, CEP existente e conectividade com ViaCEP. |
| Catálogo falha | Confira internet, logs do backend e chave/quota da Google Books. |
| Resposta 401 | Faça login novamente; chamadas privadas exigem `Authorization: Bearer TOKEN`. |
| Alteração no código não aparece | Reconstrua o serviço com `docker compose up -d --build`. |

## Observações da revisão

- O `pom.xml` define Java 21 como alvo; os Dockerfiles do backend utilizam JDK/JRE 25. O frontend usa Node 20 na imagem.
- O Hibernate mantém o esquema com `ddl-auto=update`.
- Existem testes de contratos no frontend, descritos em seu README.
- O Nginx contém regras para rotas de biblioteca/PDF que não correspondem aos controllers atuais; a consulta implementada usa `/api/livros`.
- Esta configuração é uma base local: não configura HTTPS, backup automático ou políticas de acesso por perfil. Os Dockerfiles também não possuem `.dockerignore` nesta cópia; revise o contexto de build e a inclusão de `.env` antes de distribuir imagens.

Esta documentação foi conferida contra os arquivos presentes. `docker compose config --quiet` validou a configuração local; houve aviso de acesso negado à configuração global do cliente Docker. Não foi realizado build completo nem teste integrado dos serviços durante a revisão documental.
