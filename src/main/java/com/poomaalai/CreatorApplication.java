package com.poomaalai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef="AuditorAware")
public class CreatorApplication {


	public static void main(String[] args) {
		SpringApplication.run(CreatorApplication.class, args);
	}

}
