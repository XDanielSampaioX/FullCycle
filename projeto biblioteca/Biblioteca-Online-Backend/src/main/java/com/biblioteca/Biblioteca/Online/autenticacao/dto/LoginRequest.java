package com.biblioteca.Biblioteca.Online.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "CPF e obrigatorio.")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos.")
        String cpf,

        @NotBlank(message = "Senha e obrigatoria.")
        String senha
) {
}
