package com.example.transactionsProject.service.impl;

import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.exception.*;
import com.example.transactionsProject.model.*;
import com.example.transactionsProject.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.ZoneOffset;
import org.openapitools.jackson.nullable.JsonNullable;

@RequiredArgsConstructor
@Service
public class TransaccionesApiDelegateImpl implements TransaccionesApiDelegate {

    private final ReactiveMongoRepository<Account, String> accountRepository;
    private final ReactiveMongoRepository<Transaction, String> transactionRepository;
    private final AccountRepository repository;

    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesRetiroPost(Mono<RetiroRequest> solicitudTransaccion, ServerWebExchange exchange) {
        return solicitudTransaccion.flatMap(solicitud -> {
            String origenAccountId = solicitud.getAccountId();
            double monto = solicitud.getAmount();

            return accountRepository.findById(origenAccountId)
                    .flatMap(account -> {
                        // Verificación de fondos insuficientes
                        if (account.getSaldo() < monto) {
                            // Error con mensaje detallado
                            return Mono.error(new FondosInsuficientesException("Fondos insuficientes en la cuenta."));
                        }

                        // Actualizmos el saldo de la cuenta
                        account.setSaldo(account.getSaldo() - monto);
                        return accountRepository.save(account);
                    })
                    .flatMap(account -> {
                        // Registrar la transacción
                        Transaction transaction = new Transaction("retiro", monto, account.getAccountNumber(), null);
                        return transactionRepository.save(transaction);
                    })
                    .map(transaction -> {
                        // Respuesta
                        RespuestaTransaccion respuesta = new RespuestaTransaccion();
                        respuesta.setTransactionId(transaction.getId());
                        respuesta.setTransactionType(transaction.getTypeTransaction());
                        respuesta.setAmount(transaction.getAmount());
                        respuesta.setAccountId(transaction.getOrigenAccount());
                        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));

                        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
                    })
                    .onErrorResume(e -> {
                        if (e instanceof FondosInsuficientesException) {
                            // Crear una respuesta de tipo RespuestaTransaccion con valores nulos
                            RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                            respuestaError.setTransactionType("Fondos insuficientes en su cuenta");
                            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
                        }
                        return Mono.error(e);
                    });
        });
    }

    @Override
    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesTransferenciaPost(Mono<TransferenciaRequest> solicitudTransferencia, ServerWebExchange exchange) {
        return solicitudTransferencia.flatMap(solicitud -> {
            String origenAccountNumber = solicitud.getSourceAccountId();
            String destinoAccountNumber = solicitud.getDestinationAccountId();
            double monto = solicitud.getAmount();

            // Validar que las cuentas no sean iguales
            if (origenAccountNumber.equals(destinoAccountNumber)) {
                RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                respuestaError.setTransactionType("Error");
                respuestaError.setAmount(monto);
                respuestaError.setAccountId(origenAccountNumber);
                respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountNumber));
                respuestaError.setTransactionId(null);
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
            }
                //Buscar cuenta
            return repository.findByAccountNumber(origenAccountNumber)
                    .switchIfEmpty(Mono.error(new RuntimeException("La cuenta de origen no existe.")))
                    .flatMap(cuentaOrigen -> {
                        if (cuentaOrigen.getSaldo() < monto) {
                            return Mono.error(new FondosInsuficientesException("Fondos insuficientes en la cuenta de origen."));
                        }

                        return repository.findByAccountNumber(destinoAccountNumber)
                                .switchIfEmpty(Mono.error(new RuntimeException("La cuenta de destino no existe.")))
                                .flatMap(cuentaDestino -> {
                                    cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - monto);
                                    cuentaDestino.setSaldo(cuentaDestino.getSaldo() + monto);

                                    return accountRepository.save(cuentaOrigen)
                                            .then(accountRepository.save(cuentaDestino));
                                })
                                .then(Mono.just(cuentaOrigen));
                    })
                    .flatMap(cuentaOrigen -> {
                        Transaction transaction = new Transaction("transferencia", monto, origenAccountNumber, destinoAccountNumber); // Cambiar por número de cuenta
                        return transactionRepository.save(transaction);
                    })
                    .map(transaction -> {
                        RespuestaTransaccion respuesta = new RespuestaTransaccion();
                        respuesta.setTransactionId(transaction.getId());
                        respuesta.setTransactionType(transaction.getTypeTransaction());
                        respuesta.setAmount(transaction.getAmount());
                        respuesta.setAccountId(transaction.getOrigenAccount());
                        respuesta.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccount()));
                        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));
                        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
                    })
                    .onErrorResume(e -> {

                        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                        respuestaError.setTransactionType("Error");
                        respuestaError.setAmount(monto);
                        respuestaError.setAccountId(origenAccountNumber);
                        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountNumber));
                        respuestaError.setTransactionId(null);
                        //Mostrar errore

                        if (e instanceof FondosInsuficientesException) {
                            respuestaError.setTransactionType("Fondos insuficientes en la cuenta de origen.");
                            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
                        }
                        respuestaError.setTransactionType("Error interno del servidor.");
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError));
                    });
        });

    }

    @Override
    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesDepositoPost(Mono<DepositoRequest> solicitudDeposito, ServerWebExchange exchange) {
        return solicitudDeposito.flatMap(solicitud -> {
            String destinoAccountId = solicitud.getDestinationAccountId();
            double monto = solicitud.getAmount();

            // Validar que el monto sea mayor a cero
            if (monto <= 0) {
                RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                respuestaError.setTransactionType("Error");
                respuestaError.setAmount(monto);
                respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountId));
                respuestaError.setTransactionId(null);
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
            }

            return accountRepository.findById(destinoAccountId)
                    .switchIfEmpty(Mono.error(new RuntimeException("La cuenta de destino no existe.")))
                    .flatMap(cuentaDestino -> {
                        cuentaDestino.setSaldo(cuentaDestino.getSaldo() + monto);
                        return accountRepository.save(cuentaDestino);
                    })
                    .flatMap(cuentaDestino -> {
                        Transaction transaction = new Transaction("deposito", monto, null, destinoAccountId);
                        return transactionRepository.save(transaction);
                    })
                    .map(transaction -> {
                        RespuestaTransaccion respuesta = new RespuestaTransaccion();
                        respuesta.setTransactionId(transaction.getId());
                        respuesta.setTransactionType(transaction.getTypeTransaction());
                        respuesta.setAmount(transaction.getAmount());
                        respuesta.setAccountId(null); // Sin cuenta de origen
                        respuesta.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccount()));
                        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));
                        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
                    })
                    .onErrorResume(e -> {
                        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                        respuestaError.setTransactionType("Error");
                        respuestaError.setAmount(monto);
                        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountId));
                        respuestaError.setTransactionId(null);

                        respuestaError.setTransactionType("Error interno del servidor.");
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError));
                    });
        });
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
}