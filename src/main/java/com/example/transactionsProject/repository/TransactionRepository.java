package com.example.transactionsProject.repository;
import com.example.transactionsProject.model.Transaction;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
//    @Query("{ '$or': [ { 'origenAccount': ?0 }, { 'destinationAccount': ?1 } ] }")
  //  Flux<Transaction> findByOrigenAccountOrDestinationAccount(String origenAccount, String destinationAccount);
}
