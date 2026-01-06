package com.poomaalai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@ComponentScan(basePackages = {"com.poomaalai"})
@EnableJpaAuditing(auditorAwareRef="AuditorAware")
public class CreatorApplication {


	public static void main(String[] args) {
		SpringApplication.run(CreatorApplication.class, args);
	}

}
