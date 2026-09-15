package com.biblioteca.Biblioteca.Online.usuario.dto;

public record EnderecoResponse(
        String cep,
        String logradouro,
        String rua,
        String numero,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String estado
) {
}
