package com.s3mp;

// S3mpApplication.java
// Author: Anders Engman
// Date: 6/3/22
// This is the main class which starts the server application

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S3mpApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3mpApplication.class, args);
	}

}
