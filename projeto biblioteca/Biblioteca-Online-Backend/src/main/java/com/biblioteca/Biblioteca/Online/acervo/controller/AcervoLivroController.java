package com.biblioteca.Biblioteca.Online.acervo.controller;

import com.biblioteca.Biblioteca.Online.acervo.dto.CatalogoImportacaoResponse;
import com.biblioteca.Biblioteca.Online.acervo.dto.ArquivosImportacaoResponse;
import com.biblioteca.Biblioteca.Online.acervo.dto.LivroCadastroRequest;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.acervo.service.CatalogoCcBySaService;
import com.biblioteca.Biblioteca.Online.acervo.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/acervo/livros")
@RequiredArgsConstructor
public class AcervoLivroController {

    private final LivroService livroService;
    private final CatalogoCcBySaService catalogoCcBySaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResponse cadastrarComArquivo(
            @Valid @RequestPart("livro") LivroCadastroRequest livro,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        return livroService.cadastrar(livro, arquivo);
    }

    @PostMapping(value = "/referencias", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LivroResponse> cadastrarReferencia(@Valid @RequestBody LivroCadastroRequest livro) {
        LivroService.RegistroLivro registro = livroService.cadastrarReferenciaSeAusente(livro);
        HttpStatus status = registro.criado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(registro.livro());
    }

    @PostMapping("/catalogo/cc-by-sa")
    public CatalogoImportacaoResponse importarCatalogoCcBySa() {
        return catalogoCcBySaService.importar();
    }

    @PostMapping("/catalogo/cc-by-sa/arquivos")
    public ArquivosImportacaoResponse importarArquivosDiretosDoCatalogo() {
        return catalogoCcBySaService.importarArquivosDiretos();
    }

    @PostMapping("/catalogo/cc-by-sa/gerar-pdfs")
    public ArquivosImportacaoResponse gerarPdfsDasReferenciasDoCatalogo() {
        return catalogoCcBySaService.gerarPdfsDasReferencias();
    }

    @PutMapping(value = "/{id}/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LivroResponse anexarArquivo(
            @PathVariable UUID id,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        return livroService.anexarArquivo(id, arquivo);
    }

    @PutMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> anexarCapa(
            @PathVariable UUID id,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        livroService.anexarCapa(id, arquivo);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail tratarNaoEncontrado(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail tratarFalhaNaOrigem(IllegalStateException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }
}
