package com.biblioteca.Biblioteca.Online.biblioteca.controller;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.GoogleBooksResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.service.GoogleBookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@Validated
@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
public class LivroController {

    private final GoogleBookService googleBookService;

    @GetMapping
    public GoogleBooksResponse buscar(
            @RequestParam("termo")
            @NotBlank(message = "Termo de busca e obrigatorio.")
            String termo,

            @RequestParam(name = "startIndex", defaultValue = "0")
            @Min(value = 0, message = "StartIndex deve ser maior ou igual a 0.")
            Integer startIndex,

            @RequestParam(name = "maxResults", defaultValue = "10")
            @Min(value = 1, message = "MaxResults deve ser maior ou igual a 1.")
            @Max(value = 40, message = "MaxResults deve ser menor ou igual a 40.")
            Integer maxResults
    ) {
        return googleBookService.buscarLivrosGratuitos(termo, startIndex, maxResults);
    }

    @GetMapping("/{volumeId}")
    public GoogleBooksResponse.Item buscarPorId(@PathVariable("volumeId") String volumeId) {
        return googleBookService.buscarLivroPorId(volumeId);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail tratarErroGoogleBooks(RestClientResponseException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Nao foi possivel consultar a Google Books API. Verifique a API Key configurada."
        );
    }
}
