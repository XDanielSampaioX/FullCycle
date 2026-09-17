package com.biblioteca.Biblioteca.Online.biblioteca.client;

import com.biblioteca.Biblioteca.Online.biblioteca.dto.GoogleBooksResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleBooksClient {

    private static final String VOLUMES_PATH = "/books/v1/volumes";
    private static final String FREE_EBOOKS_FILTER = "free-ebooks";

    private final RestClient restClient;
    private final String apiKey;

    public GoogleBooksClient(@Qualifier("googleBooksRestClient") RestClient restClient,
                             @Value("${google.books.api.key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public GoogleBooksResponse buscarLivrosGratuitos(String termo,
                                                     Pageable pageable) {

        return restClient.get()
                .uri(uriBuilder -> montarUriBusca(uriBuilder, termo, pageable))
                .retrieve()
                .body(GoogleBooksResponse.class);
    }

    public GoogleBooksResponse.Item buscarLivroPorId(String volumeId) {
        return restClient.get()
                .uri(uriBuilder -> montarUriPorId(uriBuilder, volumeId))
                .retrieve()
                .body(GoogleBooksResponse.Item.class);
    }

    private URI montarUriBusca(UriBuilder uriBuilder,
                               String termo,
                               Pageable pageable) {

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("termo", termo);
        variaveis.put("startIndex", pageable.getOffset());
        variaveis.put("maxResults", pageable.getPageSize());

        UriBuilder builder = uriBuilder.path(VOLUMES_PATH)
                .queryParam("q", "{termo}")
                .queryParam("filter", FREE_EBOOKS_FILTER)
                .queryParam("startIndex", "{startIndex}")
                .queryParam("maxResults", "{maxResults}");

        return montarUri(builder, variaveis);
    }

    private URI montarUriPorId(UriBuilder uriBuilder, String volumeId) {
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("volumeId", volumeId);

        UriBuilder builder = uriBuilder.path(VOLUMES_PATH)
                .pathSegment("{volumeId}");

        return montarUri(builder, variaveis);
    }

    private URI montarUri(UriBuilder uriBuilder, Map<String, Object> variaveis) {
        if (StringUtils.hasText(apiKey)) {
            uriBuilder.queryParam("key", "{apiKey}");
            variaveis.put("apiKey", apiKey);
        }

        return uriBuilder.build(variaveis);
    }
}
