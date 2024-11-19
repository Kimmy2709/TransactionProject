package com.example.transactionsProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class TransactionsProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsProjectApplication.class, args);
	}

}
