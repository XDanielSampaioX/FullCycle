package com.biblioteca.Biblioteca.Online.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
        @NotBlank(message = "CEP e obrigatorio.")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 digitos.")
        String cep,

        @NotBlank(message = "Numero e obrigatorio.")
        String numero,

        String complemento
) {
}
