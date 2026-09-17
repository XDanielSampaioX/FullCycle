package com.biblioteca.Biblioteca.Online.usuario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UsuarioExceptionHandler {

    @ExceptionHandler(UsuarioExistenteException.class)
    public ProblemDetail tratarUsuarioDuplicado(UsuarioExistenteException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());
    }

    @ExceptionHandler(ViaCepApiException.class)
    public ProblemDetail tratarErroViaCepApi(ViaCepApiException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage());
    }
}
