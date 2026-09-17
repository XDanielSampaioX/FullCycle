# Biblioteca Online — Frontend

Interface Next.js 16.3.5 com App Router, React 19.2.8, TypeScript, Tailwind CSS 4 e React Compiler habilitado. Oferece cadastro, login, catálogo de livros, detalhes com links de leitura/download e listagem de usuários.

Consulte o [README da raiz](../README.md) para obter o projeto, configurar os três `.env` e iniciar a aplicação completa.

## Executar com Docker

Na raiz do projeto, após configurar o ambiente:

```sh
docker compose up -d --build
```

Abra http://localhost. A imagem usa `node:20-alpine`, instala as dependências, executa `npm run build` e inicia com `npm start`. A porta interna é 3000, mas não está publicada no Compose; o acesso é feito pelo Nginx na porta 80. Alterações no código exigem reconstrução da imagem no Compose atual.

## Desenvolver localmente

Tenha Node.js e npm instalados; a imagem do projeto usa Node 20. O backend deve estar disponível na porta 8080, conforme seu [README](../Biblioteca-Online-Backend/README.md).

Nesta pasta, copie `.env.example` para `.env` se ainda não existir e configure:

```dotenv
INTERNAL_API_BASE_URL=http://localhost:8080
```

Instale e execute:

```sh
node --version
npm --version
npm ci
npm run dev
```

Abra http://localhost:3000. O `package-lock.json` permite instalar as dependências registradas com `npm ci`.

O cliente HTTP faz chamadas relativas `/api`. Durante o desenvolvimento local, `next.config.ts` encaminha essas chamadas para `INTERNAL_API_BASE_URL`, usando `http://localhost:8080` como padrão. Não acrescente `/api` à variável, pois o rewrite já inclui esse segmento.

No Compose, a variável é sobrescrita para `http://backend:8080`; pelo acesso normal na porta 80, o Nginx envia `/api` diretamente ao backend. Rewrites de uma compilação de produção devem ser configurados antes de `npm run build`; não suponha que alterar apenas o ambiente de `npm start` atualiza regras já compiladas. Reinicie o servidor de desenvolvimento após alterar a configuração.

Não configure senhas do banco nem segredo JWT no frontend.

## Páginas e organização

| Rota | Uso |
| --- | --- |
| `/login` | Login por CPF e senha. |
| `/cadastro` | Cadastro público com endereço. |
| `/catalogo` | Busca e paginação de livros. |
| `/catalogo/[id]` | Detalhes e links externos disponíveis. |
| `/usuarios` | Listagem de usuários. |
| `/usuarios/novo` | Cadastro dentro da área autenticada. |

- `src/app`: rotas, layouts e estilos globais.
- `src/modules/autenticacao`: sessão, contexto, hooks, login e proteção da interface.
- `src/modules/usuario`: formulários, DTOs e acesso à API de usuários.
- `src/modules/biblioteca`: busca, detalhes, DTOs e acesso à API de livros.
- `src/shared`: cliente HTTP, componentes e regras comuns de campos.
- `tests`: contratos automatizados e servidor de API simulada.

O token e sua expiração ficam em `sessionStorage`, com alternativa em memória se o armazenamento estiver indisponível. A autorização real é feita pelo backend. Não existe conta inicial; cadastre-se antes de entrar. A interface remove máscaras de CPF/CEP antes do envio e preserva o conteúdo da senha.

## Verificações e produção local

Execute nesta pasta:

```sh
npm test
npm run lint
npm run build
npm start
```

`npm start` exige build prévio e usa a porta 3000. Os testes cobrem validação de formulários, contratos HTTP, paginação, links externos e tratamento de erros, usando `fetch` simulado; não substituem testes com backend e banco reais. `npm run format` formata fontes, testes, configuração Next e este README, modificando esses arquivos. Essas verificações não foram executadas nesta revisão documental.

## Teste manual com API simulada

Para explorar a interface sem banco ou serviços externos, execute nesta pasta, em um terminal:

```sh
node tests/mock-backend.mjs
```

Em outro terminal PowerShell, também nesta pasta:

```powershell
$env:INTERNAL_API_BASE_URL = "http://127.0.0.1:18080"
npm run dev
```

Em Linux/macOS, use `INTERNAL_API_BASE_URL=http://127.0.0.1:18080 npm run dev`.

Abra http://localhost:3000 e use um CPF de 11 dígitos e senha não vazia. O simulador aceita login sem verificar credenciais reais, mantém dados em memória e retorna livros fictícios. Buscar `erro` simula falha; buscar `vazio` simula ausência de resultados. Não use esse servidor em produção. Ao terminar, encerre ambos os processos e remova a variável do terminal PowerShell com `Remove-Item Env:INTERNAL_API_BASE_URL`, ou abra outro terminal para voltar à configuração do `.env`.
