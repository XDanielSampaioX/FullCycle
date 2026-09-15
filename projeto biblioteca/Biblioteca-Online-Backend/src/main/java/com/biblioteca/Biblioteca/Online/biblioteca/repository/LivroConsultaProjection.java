package com.biblioteca.Biblioteca.Online.biblioteca.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Projecao de metadados usada nas listagens. Os campos bytea do PDF e da capa
 * ficam fora da consulta paginada para nao carregar arquivos grandes.
 */
public interface LivroConsultaProjection {

    UUID getId();

    String getTitulo();

    String getAutores();

    String getDescricao();

    String getCategoria();

    String getIdioma();

    String getLicenca();

    String getUrlLicenca();

    String getAtribuicao();

    String getUrlOrigem();

    String getUrlLeitura();

    String getUrlDownload();

    Instant getCriadoEm();

    Instant getAtualizadoEm();

    boolean getPossuiArquivo();

    String getNomeArquivo();

    String getTipoConteudo();

    Long getTamanhoBytes();

    boolean getPossuiCapa();

    String getNomeCapa();

    String getTipoCapa();

    Long getTamanhoCapaBytes();
}
