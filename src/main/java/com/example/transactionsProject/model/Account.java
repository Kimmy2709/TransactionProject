package com.example.transactionsProject.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "account")
public class Account {
    @BsonId
    private String id;
    private String accountNumber;
    private double saldo;
    private String clientId;
    private AccountType account_type;


    public enum AccountType {
        AHORROS, CORRIENTE;
    }

    public Account(String id, String accountNumber, double saldo, String clientId, AccountType account_type) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.saldo = saldo;
        this.clientId = clientId;
        this.account_type = account_type;
    }
}
