package com.biblioteca.Biblioteca.Online.acervo.dto;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;

import java.util.List;

public record CatalogoImportacaoResponse(
        int inseridos,
        int existentes,
        List<LivroResponse> livros
) {
}
