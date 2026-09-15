package com.biblioteca.Biblioteca.Online.biblioteca.service;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.GoogleBooksResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class GoogleBookService {

    private static final String VOLUMES_PATH = "/books/v1/volumes";
    private static final String FREE_EBOOKS_FILTER = "free-ebooks";
    private static final int START_INDEX_PADRAO = 0;
    private static final int MAX_RESULTS_PADRAO = 10;
    private static final int MAX_RESULTS_LIMITE = 40;

    private final RestClient restClient;
    private final String apiKey;

    public GoogleBookService(
            RestClient.Builder restClientBuilder,
            @Value("${google.books.api.base-url}") String baseUrl,
            @Value("${google.books.api.key:}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    public GoogleBooksResponse buscarLivrosGratuitos(String termo, Integer startIndex, Integer maxResults) {
        if (!StringUtils.hasText(termo)) {
            throw new IllegalArgumentException("Termo de busca e obrigatorio.");
        }

        String uri = montarUri(termo, startIndex, maxResults);

        GoogleBooksResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(GoogleBooksResponse.class);

        return response == null ? new GoogleBooksResponse(0, List.of()) : response;
    }

    public GoogleBooksResponse.Item buscarLivroPorId(String volumeId) {
        if (!StringUtils.hasText(volumeId)) {
            throw new IllegalArgumentException("ID do livro e obrigatorio.");
        }

        GoogleBooksResponse.Item response = restClient.get()
                .uri(montarUriPorId(volumeId))
                .retrieve()
                .body(GoogleBooksResponse.Item.class);

        if (response == null) {
            throw new IllegalStateException("A Google Books API nao retornou o livro solicitado.");
        }

        return response;
    }

    private String montarUri(String termo, Integer startIndex, Integer maxResults) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(VOLUMES_PATH)
                .queryParam("q", termo)
                .queryParam("filter", FREE_EBOOKS_FILTER)
                .queryParam("startIndex", normalizarStartIndex(startIndex))
                .queryParam("maxResults", normalizarMaxResults(maxResults));

        adicionarApiKey(builder);

        return builder.toUriString();
    }

    private String montarUriPorId(String volumeId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(VOLUMES_PATH)
                .pathSegment(volumeId);

        adicionarApiKey(builder);

        return builder.toUriString();
    }

    private void adicionarApiKey(UriComponentsBuilder builder) {
        if (StringUtils.hasText(apiKey)) {
            builder.queryParam("key", apiKey);
        }
    }

    private Integer normalizarStartIndex(Integer startIndex) {
        return startIndex == null || startIndex < 0 ? START_INDEX_PADRAO : startIndex;
    }

    private Integer normalizarMaxResults(Integer maxResults) {
        if (maxResults == null || maxResults < 1) {
            return MAX_RESULTS_PADRAO;
        }

        return Math.min(maxResults, MAX_RESULTS_LIMITE);
    }
}
