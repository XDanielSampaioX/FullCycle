package com.biblioteca.Biblioteca.Online.biblioteca.controller;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.GoogleBooksResponse;
import com.biblioteca.Biblioteca.Online.biblioteca.service.GoogleBookService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;

@Validated
@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
public class LivroController {

    private final GoogleBookService googleBookService;

    @GetMapping
    public GoogleBooksResponse buscar(@RequestParam("termo")
                                      @NotBlank(message = "Termo de busca e obrigatorio.")
                                      String termo,
                                      @PageableDefault(size = 10) Pageable pageable) {

        return googleBookService.buscarLivrosGratuitos(termo, pageable);
    }

    @GetMapping("/{volumeId}")
    public GoogleBooksResponse.Item buscarPorId(@PathVariable("volumeId") String volumeId) {
        return googleBookService.buscarLivroPorId(volumeId);
    }
}
