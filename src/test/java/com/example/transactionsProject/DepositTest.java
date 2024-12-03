package com.example.transactionsProject;

import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.DepositoRequest;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.Transaction;
import com.example.transactionsProject.repository.AccountRepository;
import com.example.transactionsProject.service.impl.DepositApiDelgateImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class DepositTest {


    @Mock
    private ReactiveMongoRepository<Account, String> accountRepository;

    @Mock
    private ReactiveMongoRepository<Transaction, String> transactionRepository;

    @Mock
    private AccountRepository repository;

    private DepositApiDelgateImpl depositApiDelegate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        depositApiDelegate = new DepositApiDelgateImpl(accountRepository, transactionRepository, repository);
    }
/*
    @Test
    void testTransaccionesDepositoPost_MontoNegativo() {
        // Datos de prueba
        double monto = -50.0;
        String destinoAccountId = "12345";
        DepositoRequest solicitud = new DepositoRequest(monto, destinoAccountId);

        // Llamada al método
        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = depositApiDelegate.transaccionesDepositoPost(Mono.just(solicitud), null);

        // Verificación de la respuesta
        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assertEquals(400, response.getStatusCodeValue());
                    assertTrue(response.getBody().getTransactionType().contains("El monto debe ser mayor que cero"));
                })
                .verifyComplete();
    }

    @Test
    void testTransaccionesDepositoPost_CuentaNoExistente() {
        // Datos de prueba
        double monto = 100.0;
        String destinoAccountId = "12345";
        DepositoRequest solicitud = new DepositoRequest(monto, destinoAccountId);

        // Simulamos que la cuenta no existe
        when(accountRepository.findById(destinoAccountId)).thenReturn(Mono.empty());

        // Llamada al método
        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = depositApiDelegate.transaccionesDepositoPost(Mono.just(solicitud), null);

        // Verificación de la respuesta
        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assertEquals(500, response.getStatusCodeValue());
                    assertTrue(response.getBody().getTransactionType().contains("La cuenta de destino no existe"));
                })
                .verifyComplete();
    }

    @Test
    void testTransaccionesDepositoPost_Exitoso() {
        // Datos de prueba
        double monto = 100.0;
        String destinoAccountId = "12345";
        DepositoRequest solicitud = new DepositoRequest(monto, destinoAccountId);
        Account cuentaDestino = new Account("12345", "cliente1", 500.0);
        Transaction transaction = new Transaction("deposito", monto, null, destinoAccountId);

        // Simulamos que la cuenta existe y la transacción se guarda
        when(accountRepository.findById(destinoAccountId)).thenReturn(Mono.just(cuentaDestino));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(cuentaDestino));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        // Llamada al método
        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = depositApiDelegate.transaccionesDepositoPost(Mono.just(solicitud), null);

        // Verificación de la respuesta
        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assertEquals(201, response.getStatusCodeValue());
                    assertNotNull(response.getBody().getTransactionId());
                    assertEquals("deposito", response.getBody().getTransactionType());
                    assertEquals(monto, response.getBody().getAmount());
                })
                .verifyComplete();
    }

    // Cambiar de private a protected o public
    protected Mono<ResponseEntity<RespuestaTransaccion>> crearRespuestaError(double monto, String destinoAccountId, HttpStatus status, String errorMessage) {
        RespuestaTransaccion respuestaError = new RespuestaTransaccion();
        respuestaError.setTransactionType("Error");
        respuestaError.setAmount(monto);
        respuestaError.setDestinationAccountId(JsonNullable.of(destinoAccountId));
        respuestaError.setTransactionId(null);
        respuestaError.setTransactionType(errorMessage);
        return Mono.just(ResponseEntity.status(status).body(respuestaError));
    }


    @Test
    void testTransaccionesDepositoPost() {
        double monto = 100.0;
        String destinoAccountId = "12345";
        DepositoRequest solicitud = new DepositoRequest(monto, destinoAccountId);

        Account cuentaDestino = new Account(destinoAccountId, "cliente1", 500.0);

        when(accountRepository.findById(destinoAccountId)).thenReturn(Mono.just(cuentaDestino));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(cuentaDestino));

        Transaction transaction = new Transaction("deposito", monto, null, destinoAccountId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = depositApiDelegate.transaccionesDepositoPost(Mono.just(solicitud), null);

        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assertEquals(201, response.getStatusCodeValue());
                    assertNotNull(response.getBody().getTransactionId());
                    assertEquals("deposito", response.getBody().getTransactionType());
                })
                .verifyComplete();
    }


    @Test
    void guardarTransaccionesDepositoPost() {
        // Datos de prueba
        double monto = 100.0;
        String destinoAccountId = "12345";
        DepositoRequest solicitud = new DepositoRequest(monto, destinoAccountId);

        Account cuentaDestino = new Account(destinoAccountId, "cliente1", 500.0);

        // Simulamos la llamada al repositorio para obtener la cuenta de destino
        when(accountRepository.findById(destinoAccountId)).thenReturn(Mono.just(cuentaDestino));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(cuentaDestino));

        // Simulamos la llamada al repositorio de transacciones
        Transaction transaction = new Transaction("deposito", monto, null, destinoAccountId);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        // Llamada al método
        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = depositApiDelegate.transaccionesDepositoPost(Mono.just(solicitud), null);

        // Verificación
        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assertEquals(201, response.getStatusCodeValue());
                    assertNotNull(response.getBody().getTransactionId());
                    assertEquals("deposito", response.getBody().getTransactionType());
                })
                .verifyComplete();
    }
*/
}


