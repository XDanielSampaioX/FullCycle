package com.biblioteca.Biblioteca.Online.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean("googleBooksRestClient")
    public RestClient googleBooksRestClient(RestClient.Builder restClientBuilder,
                                            @Value("${google.books.api.base-url}") String baseUrl) {
        return restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }
}
