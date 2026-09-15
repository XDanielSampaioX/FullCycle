package com.biblioteca.Biblioteca.Online.acervo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroCadastroRequest(
        @NotBlank(message = "Titulo e obrigatorio.")
        @Size(max = 250, message = "Titulo deve ter no maximo 250 caracteres.")
        String titulo,

        @NotBlank(message = "Autores e obrigatorio.")
        @Size(max = 250, message = "Autores deve ter no maximo 250 caracteres.")
        String autores,

        @Size(max = 4_000, message = "Descricao deve ter no maximo 4000 caracteres.")
        String descricao,

        @NotBlank(message = "Categoria e obrigatoria.")
        @Size(max = 120, message = "Categoria deve ter no maximo 120 caracteres.")
        String categoria,

        @NotBlank(message = "Idioma e obrigatorio.")
        @Size(max = 20, message = "Idioma deve ter no maximo 20 caracteres.")
        String idioma,

        @NotBlank(message = "Licenca e obrigatoria.")
        @Size(max = 80, message = "Licenca deve ter no maximo 80 caracteres.")
        String licenca,

        @NotBlank(message = "URL da licenca e obrigatoria.")
        @Size(max = 500, message = "URL da licenca deve ter no maximo 500 caracteres.")
        String urlLicenca,

        @NotBlank(message = "Atribuicao e obrigatoria.")
        @Size(max = 2_000, message = "Atribuicao deve ter no maximo 2000 caracteres.")
        String atribuicao,

        @NotBlank(message = "URL de origem e obrigatoria.")
        @Size(max = 1_000, message = "URL de origem deve ter no maximo 1000 caracteres.")
        String urlOrigem,

        @Size(max = 1_000, message = "URL de leitura deve ter no maximo 1000 caracteres.")
        String urlLeitura,

        @Size(max = 1_000, message = "URL de download deve ter no maximo 1000 caracteres.")
        String urlDownload
) {
}
