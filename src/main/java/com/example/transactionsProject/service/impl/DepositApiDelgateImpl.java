package com.example.transactionsProject.service.impl;

import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.DepositoRequest;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.Transaction;
import com.example.transactionsProject.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

@Primary
@RequiredArgsConstructor
@Service
public class DepositApiDelgateImpl implements TransaccionesApiDelegate {

    private final ReactiveMongoRepository<Account, String> accountRepository;
    private final ReactiveMongoRepository<Transaction, String> transactionRepository;
    private final AccountRepository repository;

    @Override
    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesDepositoPost(Mono<DepositoRequest> solicitudDeposito, ServerWebExchange exchange) {
        return solicitudDeposito.flatMap(solicitud -> {
            double monto = solicitud.getAmount();
            String destinoAccountId = solicitud.getDestinationAccountId();

            // Validar monto
            if (monto <= 0) {
                return crearRespuestaError(monto, destinoAccountId, HttpStatus.BAD_REQUEST, "Error: El monto debe ser mayor que cero.");
            }

            return accountRepository.findById(destinoAccountId)
                    .switchIfEmpty(Mono.error(new RuntimeException("La cuenta de destino no existe.")))
                    .flatMap(cuentaDestino -> procesarDeposito(cuentaDestino, monto, destinoAccountId));
        });
    }

    private Mono<ResponseEntity<RespuestaTransaccion>> crearRespuestaError(double monto, String destinoAccountId, HttpStatus status, String errorMessage) {
        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
        respuestaError.setTransactionType("Error");
        respuestaError.setAmount(monto);
        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountId));
        respuestaError.setTransactionId(null);
        respuestaError.setTransactionType(errorMessage);
        return Mono.just(ResponseEntity.status(status).body(respuestaError));
    }

    private Mono<ResponseEntity<RespuestaTransaccion>> procesarDeposito(Account cuentaDestino, double monto, String destinoAccountId) {
        cuentaDestino.setSaldo(cuentaDestino.getSaldo() + monto);
        return accountRepository.save(cuentaDestino)
                .flatMap(savedAccount -> guardarTransaccion(monto, destinoAccountId));
    }

    private Mono<ResponseEntity<RespuestaTransaccion>> guardarTransaccion(double monto, String destinoAccountId) {
        Transaction transaction = new Transaction("deposito", monto, null, destinoAccountId);
        return transactionRepository.save(transaction)
                .map(transactionGuardada -> crearRespuestaExito(transactionGuardada, monto, destinoAccountId));
    }

    private ResponseEntity<RespuestaTransaccion> crearRespuestaExito(Transaction transaction, double monto, String destinoAccountId) {
        RespuestaTransaccion respuesta = new RespuestaTransaccion();
        respuesta.setTransactionId(transaction.getId());
        respuesta.setTransactionType(transaction.getTypeTransaction());
        respuesta.setAmount(transaction.getAmount());
        respuesta.setAccountId(null); // Sin cuenta de origen
        respuesta.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccount()));
        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}

