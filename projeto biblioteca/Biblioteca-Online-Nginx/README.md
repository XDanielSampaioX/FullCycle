# Biblioteca Online — Nginx

Proxy reverso que oferece uma entrada HTTP única para frontend e API. Usa a imagem `nginx:alpine` e a configuração `nginx.conf` desta pasta.

## Executar

Instale Docker com Compose e configure os arquivos `.env` conforme o [README da raiz](../README.md). Na raiz do projeto:

```sh
docker compose up -d --build
```

Abra http://localhost. O Compose publica a porta 80 e conecta o Nginx à rede `app-network`, na qual os nomes `frontend` e `backend` são resolvidos. Não é necessário instalar Nginx diretamente na máquina nem criar um `.env` para este serviço.

## Encaminhamento

| Caminho | Destino |
| --- | --- |
| `/` e páginas da aplicação | `http://frontend:3000` |
| `/api` e seus subcaminhos | `http://backend:8080`, preservando o caminho |

O `server_name` configurado é `biblioteca-online.localhost`; para execução local, use `http://localhost`, atendido pelo único bloco de servidor disponível. A configuração não inclui certificados ou HTTPS.

Swagger e OpenAPI não estão sob `/api`; acesse-os diretamente em http://localhost:8080/swagger-ui/index.html e http://localhost:8080/v3/api-docs.

Há regras adicionais para `/api/biblioteca/livros/` e `/api/acervo/livros/catalogo/cc-by-sa/gerar-pdfs`, com timeouts de até 1800 segundos. A primeira também desabilita buffering. Não foram encontrados controllers correspondentes no backend atual; essas regras não representam funcionalidades implementadas. Os endpoints atuais de livros são `/api/livros` e `/api/livros/{volumeId}` e usam a regra geral `/api`.

## Validar e aplicar mudanças

O Compose monta `nginx.conf` como arquivo somente leitura em `/etc/nginx/nginx.conf`. Após editar a configuração, valide e recarregue, executando na raiz com o serviço iniciado:

```sh
docker compose exec nginx nginx -t
docker compose exec nginx nginx -s reload
docker compose logs --tail=100 nginx
```

Só recarregue se a validação passar. Para alterações no Dockerfile, reconstrua com `docker compose up -d --build nginx`.

Se ocorrer erro 502, confira `docker compose ps` e os logs de frontend/backend. O Compose aguarda esses contêineres iniciarem, mas não verifica se as aplicações já estão prontas. Caso um serviço seja recriado e o proxy deixe de alcançá-lo, reinicie o Nginx após confirmar que o destino está disponível.
