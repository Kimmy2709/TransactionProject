package com.example.transactionsProject;

import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.RetiroRequest;
import com.example.transactionsProject.model.Transaction;
import com.example.transactionsProject.service.impl.RetiroApiDelegateImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class RetiroTest {

    @Mock
    private ReactiveMongoRepository<Account, String> accountRepository;

    @Mock
    private ReactiveMongoRepository<Transaction, String> transactionRepository;

    @InjectMocks
    private RetiroApiDelegateImpl transaccionesApiDelegate;
/*
    @Test
    void transaccionesRetiroPost_successfulWithdrawal() {
        RetiroRequest retiroRequest = new RetiroRequest();
        retiroRequest.setAccountId("account1");
        retiroRequest.setAmount(100.0);

        Account account = new Account("1", "account1", 500.0, "client1", Account.AccountType.AHORROS);
        Transaction transaction = new Transaction("retiro", 100.0, "account1", null);
        transaction.setId("transaction1");

        Mockito.when(accountRepository.findById("account1"))
                .thenReturn(Mono.just(account));
        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(Mono.just(transaction));

        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = transaccionesApiDelegate.transaccionesRetiroPost(
                Mono.just(retiroRequest), null);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatusCode());
                    RespuestaTransaccion respuesta = response.getBody();
                    assertNotNull(respuesta);
                    assertEquals("transaction1", respuesta.getTransactionId());
                    assertEquals(100.0, respuesta.getAmount());
                    assertEquals("retiro", respuesta.getTransactionType());
                })
                .verifyComplete();
    }

    @Test
    void transaccionesRetiroPost_insufficientFunds() {
        RetiroRequest retiroRequest = new RetiroRequest();
        retiroRequest.setAccountId("account1");
        retiroRequest.setAmount(600.0);

        Account account = new Account("1", "account1", 500.0, "client1", Account.AccountType.AHORROS);

        Mockito.when(accountRepository.findById("account1"))
                .thenReturn(Mono.just(account));

        Mono<ResponseEntity<RespuestaTransaccion>> responseMono = transaccionesApiDelegate.transaccionesRetiroPost(
                Mono.just(retiroRequest), null);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    RespuestaTransaccion respuesta = response.getBody();
                    assertNotNull(respuesta);
                    assertEquals("Fondos insuficientes en su cuenta", respuesta.getTransactionType());
                })
                .verifyComplete();

 */

}

