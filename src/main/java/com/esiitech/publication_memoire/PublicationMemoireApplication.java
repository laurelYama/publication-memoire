package com.esiitech.publication_memoire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.esiitech.publication_memoire.repository")
public class PublicationMemoireApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicationMemoireApplication.class, args);
	}

}
