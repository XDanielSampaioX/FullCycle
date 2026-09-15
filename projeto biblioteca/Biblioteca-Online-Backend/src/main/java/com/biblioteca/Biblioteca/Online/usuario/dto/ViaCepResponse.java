package com.biblioteca.Biblioteca.Online.usuario.dto;

import com.biblioteca.Biblioteca.Online.usuario.domain.Endereco;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String estado,
        Boolean erro
) {

    public Endereco toEndereco() {
        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setLogradouro(logradouro);
        endereco.setRua(logradouro);
        endereco.setComplemento(complemento);
        endereco.setBairro(bairro);
        endereco.setLocalidade(localidade);
        endereco.setUf(uf);
        endereco.setEstado(estado);

        return endereco;
    }
}
