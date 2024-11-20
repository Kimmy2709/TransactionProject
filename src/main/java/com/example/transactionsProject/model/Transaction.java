package com.example.transactionsProject.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "transactions")
public class Transaction {
    @BsonId
    private String id;
    private String typeTransaction;
    private double amount;
    private LocalDateTime date;
    private String origenAccount;
    private String destinationAccount;

    public Transaction(String typeTransaction, double amount, String origenAccount, String destinationAccount) {
        this.typeTransaction = typeTransaction;
        this.amount = amount;
        this.origenAccount = origenAccount;
        this.destinationAccount = destinationAccount;
        this.date = LocalDateTime.now();
    }

}

