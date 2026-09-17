package com.biblioteca.Biblioteca.Online.usuario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UsuarioRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        String nome,

        @NotBlank(message = "CPF e obrigatorio.")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos.")
        String cpf,

        @NotBlank(message = "Senha e obrigatoria.")
        @Pattern(regexp = ".{8,}", message = "Senha deve conter pelo menos 8 caracteres.")
        String senha,

        @Valid
        @NotNull(message = "Endereco e obrigatorio.")
        EnderecoRequest endereco
) {
}
