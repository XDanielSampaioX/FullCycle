package com.biblioteca.Biblioteca.Online.biblioteca.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Representacao publica dos metadados de um livro armazenado no catalogo.
 * Os bytes do PDF e da capa nao sao incluidos nesta resposta.
 */
public record LivroResponse(
        UUID id,
        String titulo,
        String autores,
        String descricao,
        String categoria,
        String idioma,
        String licenca,
        String urlLicenca,
        String atribuicao,
        String urlOrigem,
        String urlLeitura,
        String urlDownload,
        boolean possuiArquivo,
        String nomeArquivo,
        String tipoConteudo,
        Long tamanhoBytes,
        boolean possuiCapa,
        String nomeCapa,
        String tipoCapa,
        Long tamanhoCapaBytes,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
