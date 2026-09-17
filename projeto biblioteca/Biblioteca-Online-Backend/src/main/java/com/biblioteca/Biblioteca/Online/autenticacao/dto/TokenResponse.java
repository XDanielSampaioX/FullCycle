package com.biblioteca.Biblioteca.Online.autenticacao.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
