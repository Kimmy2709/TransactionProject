package com.example.transactionsProject;

import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.RetiroRequest;
import com.example.transactionsProject.model.Transaction;
import com.example.transactionsProject.repository.AccountRepository;
import com.example.transactionsProject.service.impl.RetiroApiDelegateImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class TransactionsProjectApplicationTests {

	@Mock
	private ReactiveMongoRepository<Account, String> accountRepository;

	@Mock
	private ReactiveMongoRepository<Transaction, String> transactionRepository;

	@Mock
	private AccountRepository repository;

	@Mock
	private ServerWebExchange exchange;

	@InjectMocks
	private RetiroApiDelegateImpl transactionDelegate;

	private Account testAccount;
	private RetiroRequest testRequest;

	@BeforeEach
	void setUp() {
		// Use the constructor or builder to create the Account
		testAccount = new Account(
				"test-account-id",       // id
				"123456",                // accountNumber
				1000.0,                  // saldo
				"test-client",           // clientId
				Account.AccountType.AHORROS // account_type
		);

		// Prepare a withdrawal request
		testRequest = new RetiroRequest();
		testRequest.setAccountId("test-account-id");
		testRequest.setAmount(500.0);
	}
/*
	@Test
	void testSuccessfulWithdrawal() {
		lenient().when(accountRepository.findById(testRequest.getAccountId()))
				.thenReturn(Mono.just(testAccount));

		Account updatedAccount = new Account(
				"test-account-id",
				"123456",
				500.0,
				"test-client",
				Account.AccountType.AHORROS
		);

		lenient().when(accountRepository.save(any(Account.class)))
				.thenReturn(Mono.just(updatedAccount));

		Transaction savedTransaction = new Transaction(
				"retiro",
				testRequest.getAmount(),
				testAccount.getAccountNumber(),
				null
		);
		savedTransaction.setId("test-transaction-id");
		savedTransaction.setDate(LocalDateTime.now());

		lenient().when(transactionRepository.save(any(Transaction.class)))
				.thenReturn(Mono.just(savedTransaction));

		Mono<ResponseEntity<RespuestaTransaccion>> resultMono =
				transactionDelegate.transaccionesRetiroPost(Mono.just(testRequest), exchange);

		StepVerifier.create(resultMono)
				.assertNext(response -> {
					assertEquals(HttpStatus.CREATED, response.getStatusCode());

					RespuestaTransaccion respuesta = response.getBody();
					assertNotNull(respuesta);
					assertEquals("retiro", respuesta.getTransactionType());
					assertEquals(500.0, respuesta.getAmount());
					assertEquals("test-account-id", respuesta.getAccountId());
					assertEquals("test-transaction-id", respuesta.getTransactionId());
					assertNotNull(respuesta.getDate());
				})
				.verifyComplete();
	}

	@Test
	void testInsufficientFunds() {
		testAccount.setSaldo(100.0);
		testRequest.setAmount(500.0);

		when(accountRepository.findById(testRequest.getAccountId()))
				.thenReturn(Mono.just(testAccount));

		Mono<ResponseEntity<RespuestaTransaccion>> resultMono =
				transactionDelegate.transaccionesRetiroPost(Mono.just(testRequest), exchange);

		StepVerifier.create(resultMono)
				.assertNext(response -> {
					assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

					RespuestaTransaccion respuesta = response.getBody();
					assertNotNull(respuesta);
					assertEquals("Fondos insuficientes en su cuenta", respuesta.getTransactionType());
				})
				.verifyComplete();
	}

	@Test
	void testAccountNotFound() {
		// Simulate account not found scenario
		when(accountRepository.findById(testRequest.getAccountId()))
				.thenReturn(Mono.empty());

		// Execute the method
		Mono<ResponseEntity<RespuestaTransaccion>> resultMono =
				transactionDelegate.transaccionesRetiroPost(Mono.just(testRequest), exchange);

		// Verify that an error is propagated
		StepVerifier.create(resultMono)
				.expectError()
				.verify();
	}
*/

}

