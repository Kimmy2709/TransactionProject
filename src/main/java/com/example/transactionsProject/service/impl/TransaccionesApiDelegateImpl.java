package com.example.transactionsProject.service.impl;

import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.exception.FondosInsuficientesException;
import com.example.transactionsProject.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransaccionesApiDelegateImpl implements TransaccionesApiDelegate {

    private final ReactiveMongoRepository<Account, String> accountRepository;
    private final ReactiveMongoRepository<Transaction, String> transactionRepository;

    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesRetiroPost(Mono<SolicitudTransaccion> solicitudTransaccion, ServerWebExchange exchange) {
        return solicitudTransaccion.flatMap(solicitud -> {
            String origenAccountId = solicitud.getAccountId();
            double monto = solicitud.getAmount();

            return accountRepository.findById(origenAccountId)
                    .flatMap(account -> {
                        // Verificación de fondos insuficientes
                        if (account.getSaldo() < monto) {
                            // Aquí lanzamos el error con un mensaje más detallado
                            return Mono.error(new FondosInsuficientesException("Fondos insuficientes en la cuenta."));
                        }

                        // Actualizamos el saldo de la cuenta
                        account.setSaldo(account.getSaldo() - monto);
                        return accountRepository.save(account);
                    })
                    .flatMap(account -> {
                        // Registrar la transacción
                        Transaction transaction = new Transaction("retiro", monto, account.getAccountNumber(), null);
                        return transactionRepository.save(transaction);
                    })
                    .map(transaction -> {
                        // Crear la respuesta
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
                            // Crear una respuesta de tipo RespuestaTransaccion con valores nulos o por defecto
                            RespuestaTransaccion respuestaError = new RespuestaTransaccion();
                            respuestaError.setTransactionType("Fondos insuficientes en su cuenta");
                            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError));
                        }
                        return Mono.error(e);
                    });
        });
    }
}