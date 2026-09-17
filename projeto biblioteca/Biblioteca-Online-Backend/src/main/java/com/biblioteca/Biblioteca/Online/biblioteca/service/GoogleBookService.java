package com.biblioteca.Biblioteca.Online.biblioteca.service;

import com.biblioteca.Biblioteca.Online.biblioteca.client.GoogleBooksClient;
import com.biblioteca.Biblioteca.Online.biblioteca.dto.GoogleBooksResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GoogleBookService {

    private final GoogleBooksClient googleBooksClient;

    public GoogleBookService(GoogleBooksClient googleBooksClient) {
        this.googleBooksClient = googleBooksClient;
    }

    public GoogleBooksResponse buscarLivrosGratuitos(String termo, Pageable pageable) {
        if (!StringUtils.hasText(termo)) {
            throw new IllegalArgumentException("Termo de busca e obrigatorio.");
        }

        GoogleBooksResponse response = googleBooksClient.buscarLivrosGratuitos(termo, pageable);

        return response == null ? new GoogleBooksResponse(0, List.of()) : response;
    }

    public GoogleBooksResponse.Item buscarLivroPorId(String volumeId) {
        if (!StringUtils.hasText(volumeId)) {
            throw new IllegalArgumentException("ID do livro e obrigatorio.");
        }

        GoogleBooksResponse.Item response = googleBooksClient.buscarLivroPorId(volumeId);

        if (response == null) {
            throw new IllegalStateException("A Google Books API nao retornou o livro solicitado.");
        }

        return response;
    }

}
