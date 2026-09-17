package com.biblioteca.Biblioteca.Online.autenticacao.controller;

import com.biblioteca.Biblioteca.Online.autenticacao.dto.LoginRequest;
import com.biblioteca.Biblioteca.Online.autenticacao.dto.TokenResponse;
import com.biblioteca.Biblioteca.Online.autenticacao.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.autenticar(request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public void tratarCredenciaisInvalidas() {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
    }
}
