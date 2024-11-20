package com.example.transactionsProject.controller;

import com.example.transactionsProject.api.TransaccionesApi;
import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.RetiroRequest;
import com.example.transactionsProject.model.TransferenciaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transacciones")
public class TransactionController implements TransaccionesApi {

    private final TransaccionesApiDelegate delegate;

    @Autowired
    public TransactionController(TransaccionesApiDelegate delegate) {
        this.delegate = delegate;
    }

    @PostMapping("/retiro")
    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesRetiroPost(@RequestBody Mono<RetiroRequest> solicitudRetiro) {
        return delegate.transaccionesRetiroPost(solicitudRetiro, null);
    }

    @PostMapping("/transferencia")
    public Mono<ResponseEntity<RespuestaTransaccion>> realizarTransferencia(@RequestBody Mono<TransferenciaRequest> solicitudTransferencia, ServerWebExchange exchange) {
        return delegate.transaccionesTransferenciaPost(solicitudTransferencia, exchange);
    }

    @PostMapping("/deposito")
    public Mono<ResponseEntity<RespuestaTransaccion>> realizarDeposito(@RequestBody Mono<TransferenciaRequest> solicitudDeposito, ServerWebExchange exchange) {
        return delegate.transaccionesTransferenciaPost(solicitudDeposito, exchange);
    }


}