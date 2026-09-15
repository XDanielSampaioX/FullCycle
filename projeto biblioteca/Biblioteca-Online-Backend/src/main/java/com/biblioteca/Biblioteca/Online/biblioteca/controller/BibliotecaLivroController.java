package com.biblioteca.Biblioteca.Online.biblioteca.controller;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.service.LivroConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/biblioteca/livros")
@RequiredArgsConstructor
public class BibliotecaLivroController {

    private final LivroConsultaService livroConsultaService;

    @GetMapping
    public Page<LivroResponse> buscar(
            @RequestParam(name = "termo", required = false) String termo,
            @PageableDefault(size = 20, sort = {"titulo", "id"}) Pageable pageable
    ) {
        return livroConsultaService.buscar(termo, pageable);
    }

    @GetMapping("/{id}")
    public LivroResponse buscarPorId(@PathVariable UUID id) {
        return livroConsultaService.buscarPorId(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable UUID id) {
        LivroConsultaService.ArquivoLivro arquivo = livroConsultaService.baixarArquivo(id);
        return responderArquivo(arquivo);
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<byte[]> baixarCapa(@PathVariable UUID id) {
        return responderArquivo(livroConsultaService.baixarCapa(id));
    }

    private ResponseEntity<byte[]> responderArquivo(LivroConsultaService.ArquivoLivro arquivo) {

        MediaType tipoConteudo;
        try {
            tipoConteudo = MediaType.parseMediaType(arquivo.tipoConteudo());
        } catch (IllegalArgumentException exception) {
            tipoConteudo = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(tipoConteudo)
                .contentLength(arquivo.conteudo().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(arquivo.nomeArquivo(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(arquivo.conteudo());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail tratarNaoEncontrado(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
