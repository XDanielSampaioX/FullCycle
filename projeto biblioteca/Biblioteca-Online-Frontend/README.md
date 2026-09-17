# Biblioteca Online — Frontend

Frontend Next.js com TypeScript estrito. Os módulos de negócio espelham o backend atual: `autenticacao`, `usuario` e `biblioteca`.

## Executar

Requer Node.js 20.9+ e o backend em execução.

```sh
npm install
npm run dev
```

Abra http://localhost:3000. Por padrão, o Next encaminha `/api/*` para `http://localhost:8080/api/*`. Para outro destino, configure `INTERNAL_API_BASE_URL` no `.env.local` e reinicie o frontend. A variável é do servidor e não deve conter `/api` no final. Em builds de produção, configure-a antes de `npm run build`.

No Docker Compose existente, `INTERNAL_API_BASE_URL=http://backend:8080` já está configurado. O Nginx encaminha `/api` diretamente ao backend e as demais rotas ao frontend.

## Páginas

| Rota             | Acesso      | Função                                           |
| ---------------- | ----------- | ------------------------------------------------ |
| `/login`         | Público     | Login por CPF e senha                            |
| `/cadastro`      | Público     | Criação de conta                                 |
| `/catalogo`      | Autenticado | Busca e paginação de livros gratuitos            |
| `/catalogo/[id]` | Autenticado | Detalhes, leitura e downloads quando disponíveis |
| `/usuarios`      | Autenticado | Listagem de usuários                             |
| `/usuarios/novo` | Autenticado | Cadastro de outros usuários                      |

## Organização

- `src/app`: rotas e layouts; delegam a apresentação aos módulos.
- `src/modules/<modulo>/dto`: contratos e construção de entradas válidas.
- `src/modules/<modulo>/services`: chamadas dos endpoints do módulo.
- `src/modules/<modulo>/components`: telas e componentes específicos.
- `src/modules/autenticacao/session.ts`: armazenamento, leitura, notificações, início e encerramento da sessão.
- `src/modules/autenticacao/hooks`: hooks próprios `useSessao` e `useConsulta`, para acompanhar a sessão e realizar consultas autenticadas com cancelamento e tratamento de 401.
- `src/modules/autenticacao/components`: formulário de login e proteção da navegação autenticada.
- `src/shared`: transporte HTTP, componentes visuais e regras de campos reutilizáveis.

Normalização e validação ficam nos DTOs e objetos de parâmetros, seguindo a skill `validacao-no-dominio`. Serviços não corrigem entradas. Login e cadastro compartilham o contrato de credenciais; cadastro público e interno compartilham o formulário. Não há módulos antigos de acervo, pois eles não fazem parte do backend atual.

## Contratos consumidos

- `POST /api/auth/login`: `{ cpf, senha }` → `{ accessToken, tokenType, expiresIn }`.
- `POST /api/usuarios`: `{ nome, cpf, senha, endereco: { cep, numero, complemento } }` → `UsuarioResponse`.
- `GET /api/usuarios`: lista de `UsuarioResponse`.
- `GET /api/livros?termo=...&page=0&size=12`: `{ totalItems, items }`, com campos aninhados de Google Books, sem envelope de paginação Spring.
- `GET /api/livros/{volumeId}`: item de Google Books.

CPF e CEP aceitam máscara na tela, mas são enviados com 11 e 8 dígitos. A senha é preservada exatamente como digitada; no cadastro deve ter pelo menos 8 caracteres. O endereço completo é resolvido pelo backend usando o CEP.

O JWT é enviado como `Authorization: Bearer ...` nas consultas protegidas. A sessão fica em `sessionStorage`, por aba, e expira conforme `expiresIn`; logout e respostas 401 encerram a sessão. A proteção real dos dados continua no Spring Security. A guarda do frontend impede a exibição das telas privadas antes de carregar a sessão. Como o token é acessível ao JavaScript da aplicação, não se deve inserir HTML de fontes externas sem sanitização; descrições dos livros são exibidas como texto.

O cadastro é público no contrato atual, e não existe distinção de administrador. Todos os usuários autenticados podem listar e cadastrar usuários, conforme o backend. Erros com `ProblemDetail.detail` são exibidos; quando o backend não informa a causa, a interface apresenta uma mensagem genérica. Duplicidade de CPF e CEP inexistente atualmente podem chegar como erro genérico do servidor.

## Verificações

```sh
npm test
npm run lint
npm run build
```

Os testes de contrato usam respostas simuladas, sem criar usuários no banco. Cobrem os payloads e endpoints, envio de Bearer, paginação, campos formatados, senha, links externos e falhas HTTP/rede. A busca real depende da configuração da API Google Books no backend.
