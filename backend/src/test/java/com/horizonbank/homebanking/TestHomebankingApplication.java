package com.horizonbank.homebanking;

import org.springframework.boot.SpringApplication;

public class TestHomebankingApplication {

	public static void main(String[] args) {
		SpringApplication.from(HomebankingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
