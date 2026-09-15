package com.biblioteca.Biblioteca.Online.usuario.controller;

import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioRequest;
import com.biblioteca.Biblioteca.Online.usuario.dto.UsuarioResponse;
import com.biblioteca.Biblioteca.Online.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.cadastrar(request);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }
}
