package com.biblioteca.Biblioteca.Online.biblioteca.exception;

public class GoogleBooksApiException extends RuntimeException {
    public GoogleBooksApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
