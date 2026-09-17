package com.biblioteca.Biblioteca.Online.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LivroExceptionHandler {

    @ExceptionHandler(GoogleBooksApiException.class)
    public ProblemDetail tratarExceptionGoogleBooks(GoogleBooksApiException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage()
        );
    }
}
