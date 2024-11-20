package com.example.transactionsProject.exception;

public class FondosInsuficientesException extends RuntimeException {
    //Manejar mensajes de error
    public FondosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}
