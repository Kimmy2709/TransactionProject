package com.example.transactionsProject.service.impl;

import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.exception.*;
import com.example.transactionsProject.model.*;
import com.example.transactionsProject.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Service
public class RetiroApiDelegateImpl implements TransaccionesApiDelegate {

    private final ReactiveMongoRepository<Account, String> accountRepository;
    private final ReactiveMongoRepository<Transaction, String> transactionRepository;
    private final AccountRepository repository;

    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesRetiroPost(
            Mono<RetiroRequest> solicitudTransaccion, ServerWebExchange exchange) {
        return solicitudTransaccion.flatMap(solicitud ->
                validarCuentaYFondos(solicitud)
                        .flatMap(cuenta -> actualizarSaldoCuenta(cuenta, solicitud.getAmount()))
                        .flatMap(cuenta -> registrarTransaccion(cuenta, solicitud.getAmount()))
                        .map(this::prepararRespuesta)
                        .onErrorResume(this::manejarErrores)
        );
    }

    private Mono<Account> validarCuentaYFondos(RetiroRequest solicitud) {
        return accountRepository.findById(solicitud.getAccountId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    if (account.getSaldo() < solicitud.getAmount()) {
                        return Mono.error(new FondosInsuficientesException("Fondos insuficientes en la cuenta."));
                    }
                    return Mono.just(account);
                });
    }

    private Mono<Account> actualizarSaldoCuenta(Account account, double monto) {
        account.setSaldo(account.getSaldo() - monto);
        return accountRepository.save(account)
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to update account balance")));
    }

    private Mono<Transaction> registrarTransaccion(Account account, double monto) {
        Transaction transaction = new Transaction(
                "retiro", monto, account.getAccountNumber(), null);
        return transactionRepository.save(transaction)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction save failed")));
    }

    private ResponseEntity<RespuestaTransaccion> prepararRespuesta(Transaction transaction) {
        RespuestaTransaccion respuesta = new RespuestaTransaccion();
        respuesta.setTransactionId(transaction.getId());
        respuesta.setTransactionType(transaction.getTypeTransaction());
        respuesta.setAmount(transaction.getAmount());
        respuesta.setAccountId(transaction.getId());
        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    private Mono<ResponseEntity<RespuestaTransaccion>> manejarErrores(Throwable e) {
        if (e instanceof FondosInsuficientesException) {
            RespuestaTransaccion respuestaError = new RespuestaTransaccion();
            respuestaError.setTransactionType("Fondos insuficientes en su cuenta");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
        }
        return Mono.error(e);
    }
}
/*
    @Override
    public Mono<ResponseEntity<Flux<RespuestaTransaccion>>> transaccionesHistorialGet(String accountId, ServerWebExchange exchange) {
        Flux<Transaction> transacciones;

        transacciones = transacRepository.findByOrigenAccount(accountId);

        return transacciones
                .map(transaction -> {
                    RespuestaTransaccion respuesta = new RespuestaTransaccion();
                    respuesta.setTransactionId(transaction.getId());
                    respuesta.setTransactionType(transaction.getTypeTransaction());
                    respuesta.setAmount(transaction.getAmount());
                    respuesta.setAccountId(transaction.getOrigenAccount()); // o destinationAccount
                    respuesta.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccount()));
                    respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));

                    return respuesta;
                })
                .collectList()
                .map(respuestas -> ResponseEntity.ok(Flux.fromIterable(respuestas)));
    }*/
