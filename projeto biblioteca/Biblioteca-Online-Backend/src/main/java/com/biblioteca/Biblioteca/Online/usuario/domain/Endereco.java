package com.biblioteca.Biblioteca.Online.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Endereco {

    @Column(length = 9)
    private String cep;

    private String logradouro;

    private String rua;

    private String numero;

    private String complemento;

    private String bairro;

    private String localidade;

    @Column(length = 2)
    private String uf;

    private String estado;
}
