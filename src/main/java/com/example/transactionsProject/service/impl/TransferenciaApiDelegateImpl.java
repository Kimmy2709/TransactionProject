package com.example.transactionsProject.service.impl;

import com.example.transactionsProject.api.TransaccionesApiDelegate;
import com.example.transactionsProject.exception.FondosInsuficientesException;
import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.Transaction;
import com.example.transactionsProject.model.TransferenciaRequest;
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


@RequiredArgsConstructor
@Service
public class TransferenciaApiDelegateImpl implements TransaccionesApiDelegate {
    private final ReactiveMongoRepository<Account, String> accountRepository;
    private final ReactiveMongoRepository<Transaction, String> transactionRepository;
    private final AccountRepository repository;

    @Override
    public Mono<ResponseEntity<RespuestaTransaccion>> transaccionesTransferenciaPost(
            Mono<TransferenciaRequest> solicitudTransferencia, ServerWebExchange exchange) {

        return solicitudTransferencia.flatMap(solicitud -> {
            String origenAccountNumber = solicitud.getSourceAccountId();
            String destinoAccountNumber = solicitud.getDestinationAccountId();
            double monto = solicitud.getAmount();

            // Validar cuentas
            return validarCuentas(origenAccountNumber, destinoAccountNumber, monto)
                    .switchIfEmpty(
                            obtenerCuenta(origenAccountNumber)
                                    .flatMap(cuentaOrigen -> verificarFondosYRealizarTransferencia(cuentaOrigen, destinoAccountNumber, monto))
                                    .flatMap(cuentaOrigen -> crearYGuardarTransaccion(monto, origenAccountNumber, destinoAccountNumber))
                                    .flatMap(transaction -> prepararRespuestaTransaccion(transaction))
                    )
                    .onErrorResume(e -> {
                        return Mono.just(manejarErrores(e, monto, origenAccountNumber, destinoAccountNumber));
                    });
        });
    }

    // Método para validar que las cuentas no sean iguales
    private Mono<ResponseEntity<RespuestaTransaccion>> validarCuentas(String origenAccountNumber, String destinoAccountNumber, double monto) {
        if (origenAccountNumber.equals(destinoAccountNumber)) {
            return Mono.just(crearRespuestaError("Error", monto, origenAccountNumber, destinoAccountNumber, null, HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    // Método para obtener una cuenta por su número
    private Mono<Account> obtenerCuenta(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new RuntimeException("Cuenta no encontrada.")));
    }

    // Verifica si la cuenta tiene fondos suficientes y realiza la transferencia
    private Mono<Account> verificarFondosYRealizarTransferencia(Account cuentaOrigen, String destinoAccountNumber, double monto) {
        if (cuentaOrigen.getSaldo() < monto) {
            return Mono.error(new FondosInsuficientesException("Fondos insuficientes en la cuenta de origen."));
        }
        return obtenerCuenta(destinoAccountNumber)
                .flatMap(cuentaDestino -> {
                    cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - monto);
                    cuentaDestino.setSaldo(cuentaDestino.getSaldo() + monto);
                    return accountRepository.save(cuentaOrigen)
                            .then(accountRepository.save(cuentaDestino))
                            .then(Mono.just(cuentaOrigen)); // Retorna la cuenta origen actualizada
                });
    }

    // Método para crear y guardar la transacción
    private Mono<Transaction> crearYGuardarTransaccion(double monto, String origenAccountNumber, String destinoAccountNumber) {
        Transaction transaction = new Transaction("transferencia", monto, origenAccountNumber, destinoAccountNumber);
        return transactionRepository.save(transaction);
    }

    // Método para preparar la respuesta de la transacción
    private Mono<ResponseEntity<RespuestaTransaccion>> prepararRespuestaTransaccion(Transaction transaction) {
        RespuestaTransaccion respuesta = new RespuestaTransaccion();
        respuesta.setTransactionId(transaction.getId());
        respuesta.setTransactionType(transaction.getTypeTransaction());
        respuesta.setAmount(transaction.getAmount());
        respuesta.setAccountId(transaction.getOrigenAccount());
        respuesta.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccount()));
        respuesta.setDate(transaction.getDate().atOffset(ZoneOffset.UTC));

        return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(respuesta));
    }

    // Método para manejar los errores
    private ResponseEntity<RespuestaTransaccion> manejarErrores(Throwable e, double monto, String origenAccountNumber, String destinoAccountNumber) {
        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
        respuestaError.setTransactionType("Error");
        respuestaError.setAmount(monto);
        respuestaError.setAccountId(origenAccountNumber);
        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountNumber));
        respuestaError.setTransactionId(null);

        if (e instanceof FondosInsuficientesException) {
            respuestaError.setTransactionType("Fondos insuficientes en la cuenta de origen.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
        }
        respuestaError.setTransactionType("Error interno del servidor.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
    }

    // Método para crear una respuesta de error
    private ResponseEntity<RespuestaTransaccion> crearRespuestaError(String tipoTransaccion, double monto, String origenAccountNumber, String destinoAccountNumber, String transactionId, HttpStatus status) {
        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
        respuestaError.setTransactionType(tipoTransaccion);
        respuestaError.setAmount(monto);
        respuestaError.setAccountId(origenAccountNumber);
        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountNumber));
        respuestaError.setTransactionId(transactionId);
        return ResponseEntity.status(status).body(respuestaError);
    }


}
