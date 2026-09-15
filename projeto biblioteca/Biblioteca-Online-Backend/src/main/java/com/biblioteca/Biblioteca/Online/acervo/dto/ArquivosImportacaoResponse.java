package com.biblioteca.Biblioteca.Online.acervo.dto;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.LivroResponse;

import java.util.List;
import java.util.UUID;

public record ArquivosImportacaoResponse(
        int importados,
        int existentes,
        List<LivroResponse> livros,
        List<FalhaImportacao> falhas
) {

    public record FalhaImportacao(
            UUID livroId,
            String titulo,
            String urlDownload,
            String motivo
    ) {
    }
}
