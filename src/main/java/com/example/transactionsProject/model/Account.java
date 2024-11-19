package com.example.transactionsProject.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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

    public enum AccountType{
        AHORROS, CORRIENTE;
    }
}
