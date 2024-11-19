package com.example.transactionsProject.repository;

import com.example.transactionsProject.model.Account;
import com.example.transactionsProject.model.RespuestaTransaccion;
import com.example.transactionsProject.model.SolicitudTransaccion;
import com.example.transactionsProject.model.Transaction;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Mono<Transaction> findByTypeTransaction(String typeTransaction);

}
