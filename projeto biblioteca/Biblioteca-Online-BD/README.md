# Biblioteca Online — Banco de dados

Serviço PostgreSQL 16 usado pelo backend para persistir usuários e endereços. O Dockerfile deriva de `postgres:16`; não contém scripts SQL próprios nem carga inicial. O esquema é mantido pelo Hibernate do backend com `ddl-auto=update`.

## Configurar

Instale Docker com Compose e siga o [guia da raiz](../README.md). Copie `.env.example` para `.env` nesta pasta se ainda não existir e preencha as variáveis vazias, por exemplo:

```dotenv
POSTGRES_DB=biblioteca
POSTGRES_USER=biblioteca
POSTGRES_PASSWORD=troque_esta_senha_local
```

Configure `SPRING_DATASOURCE_*` no `.env` do backend com banco, usuário e senha correspondentes. Não publique credenciais no Git. As variáveis `POSTGRES_*` são usadas na primeira inicialização de um diretório de dados vazio; editar o `.env` não altera as credenciais de um banco existente.

## Iniciar e acessar

Na raiz do projeto, com os três `.env` exigidos pelo Compose preparados:

```sh
docker compose up -d postgres
docker compose ps postgres
docker compose logs -f postgres
```

O Compose publica `5432:5432`. Em um cliente de banco instalado na máquina, conecte a `localhost:5432` usando as credenciais do `.env`. Dentro da rede Docker, o backend usa `postgres:5432`.

Para abrir o cliente SQL no próprio contêiner, execute na raiz e substitua banco e usuário se tiver usado outros valores:

```sh
docker compose exec postgres psql -U biblioteca -d biblioteca
```

O Compose verifica a disponibilidade com `pg_isready` a cada 5 segundos, com timeout de 5 segundos e 10 tentativas. O backend aguarda essa condição antes de iniciar.

## Persistência e parada

O volume nomeado `postgres_data` é montado em `/var/lib/postgresql/data`. Seu nome efetivo depende do prefixo do projeto Compose. Reconstruir a imagem não remove esse volume.

```sh
docker compose stop postgres
docker compose start postgres
```

`docker compose down`, na raiz, encerra a composição preservando o volume. **`docker compose down -v` apaga os dados persistidos**, portanto não é um comando de parada rotineira. Não há backup automático configurado; preserve uma cópia antes de intervenções no banco.

Se o serviço não iniciar, verifique a porta 5432, as variáveis obrigatórias e os logs. Se a conexão falhar após uma troca de senha no `.env`, ajuste também o usuário do banco existente; não apague o volume para corrigir credenciais quando precisar preservar os dados.
