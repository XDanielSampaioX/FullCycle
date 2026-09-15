# Acervo de livros

O acervo local fica separado da consulta da Google Books. A consulta da Google Books continua em `/api/livros`; os livros persistidos sao consultados pelo modulo `biblioteca` em `/api/biblioteca/livros`.

## Importar o catalogo

O primeiro endpoint cadastra os 25 livros e seus metadados, de forma idempotente:

```http
POST http://localhost:8080/api/acervo/livros/catalogo/cc-by-sa
```

O endpoint abaixo faz o mesmo cadastro e baixa somente os PDFs oficiais que possuem uma URL direta validada no catalogo (10 arquivos). Os demais entram como referencia de leitura/fonte, pois nao possuem PDF oficial pronto:

```http
POST http://localhost:8080/api/acervo/livros/catalogo/cc-by-sa/arquivos
```

O resultado informa `importados`, `existentes` e `falhas` por livro. Uma origem indisponivel nao interrompe os outros downloads.

## Gerar PDF para referencias sem PDF direto

Para os livros cujo repositorio possui apenas Markdown, RST, RMarkdown, Quarto,
Jupyter Notebook ou Org-mode, a API tambem consegue baixar o ZIP do GitHub,
extrair os arquivos textuais e montar um PDF estatico:

```http
POST http://localhost:8080/api/acervo/livros/catalogo/cc-by-sa/gerar-pdfs
```

Esse processo e sincrono e pode levar varios minutos. Ele nao executa scripts dos
repositorios; somente converte arquivos textuais. Os livros que dependem de
interativos, imagens ou componentes externos ficam sujeitos a uma versao
simplificada do conteudo.

Os arquivos sao validados pela extensao, assinatura `%PDF-`, tamanho (500 MB) e SHA-256 antes de serem gravados no PostgreSQL.

## Enviar um PDF manualmente

Cadastre apenas os metadados:

```http
POST http://localhost:8080/api/acervo/livros/referencias
Content-Type: application/json
```

Depois, anexe o arquivo ao `id` retornado:

```http
PUT http://localhost:8080/api/acervo/livros/{id}/arquivo
Content-Type: multipart/form-data
```

Para salvar a capa no banco, envie uma imagem pelo endpoint do acervo:

```http
PUT http://localhost:8080/api/acervo/livros/{id}/capa
Content-Type: multipart/form-data
```

Esse endpoint grava a imagem e responde `204 No Content`.

O PDF fica em `tb_livro_arquivo` e a imagem em `tb_livro_capa`; os bytes nao
participam da consulta paginada de metadados. O DTO informa apenas a existencia,
o nome, o tipo e o tamanho de cada arquivo.

Na primeira inicializacao depois desta separacao, os PDFs que ainda estiverem
nas colunas antigas de `tb_livro` sao copiados automaticamente para
`tb_livro_arquivo`.

Com `curl.exe` no Windows:

```powershell
$metadata = '{"titulo":"Livro de teste","autores":"Autor","categoria":"Programacao","idioma":"pt-BR","licenca":"CC BY-SA 4.0","urlLicenca":"https://creativecommons.org/licenses/by-sa/4.0/","atribuicao":"Autor, Livro de teste, CC BY-SA 4.0","urlOrigem":"https://exemplo.org/livro"}'

curl.exe -X POST "http://localhost:8080/api/acervo/livros" `
  -F "livro=$metadata;type=application/json" `
  -F "arquivo=@C:\caminho\livro.pdf;type=application/pdf"
```

## Consultar e baixar pelo modulo biblioteca

```http
GET http://localhost:8080/api/biblioteca/livros?page=0&size=20
GET http://localhost:8080/api/biblioteca/livros?termo=python&page=0&size=20
GET http://localhost:8080/api/biblioteca/livros/{id}
GET http://localhost:8080/api/biblioteca/livros/{id}/download
GET http://localhost:8080/api/biblioteca/livros/{id}/capa
```

O GET de listagem usa a paginação nativa do Spring Data (`page`, `size` e `sort`). O tamanho padrão e 20 registros. O campo opcional `termo` procura por titulo, autor ou categoria e e enviado ao repositorio sem alteracao. A aplicação serializa `Page` usando `PageSerializationMode.VIA_DTO`.

O DTO nunca devolve os bytes do PDF na listagem. A resposta informa `possuiArquivo`, `nomeArquivo`, `tamanhoBytes` e o endpoint separado de download. O modulo `acervo` permanece apenas com as rotinas de importacao e anexacao de arquivos e pode ser removido depois que o catalogo estiver pronto.

Todos os registros do catalogo carregam a atribuição e a URL da licença **CC BY-SA 4.0**. Se um arquivo for adaptado, a versão derivada deve manter essa licença.
