package com.biblioteca.Biblioteca.Online.usuario.service;

import com.biblioteca.Biblioteca.Online.usuario.domain.Endereco;
import com.biblioteca.Biblioteca.Online.usuario.dto.ViaCepResponse;
import com.biblioteca.Biblioteca.Online.usuario.exception.ViaCepApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient.Builder restClientBuilder;

    public Endereco buscarPorCep(String cep) {
        try {
            ViaCepResponse response = restClientBuilder
                    .build()
                    .get()
                    .uri(VIACEP_URL, cep)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null || Boolean.TRUE.equals(response.erro())) {
                throw new IllegalArgumentException("CEP nao encontrado.");
            }

            return response.toEndereco();

        } catch (RestClientException exception) {
            throw new ViaCepApiException("Erro ao consultar a API ViaCEP.", exception);
        }
    }
}
