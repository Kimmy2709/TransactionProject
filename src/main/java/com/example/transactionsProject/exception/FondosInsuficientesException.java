package com.example.transactionsProject.exception;

public class FondosInsuficientesException extends RuntimeException {
    public FondosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}
