package com.poomaalai;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@ComponentScan(basePackages = {"com.poomaalai"})
@EnableJpaAuditing(auditorAwareRef="auditorAware")
public class CreatorApplication {

	private static final Logger logger = LoggerFactory.getLogger(CreatorApplication.class);

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(CreatorApplication.class, args);
		
		// Log all registered endpoints
		logger.info("========== Registered Endpoints ==========");
		RequestMappingHandlerMapping requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		map.forEach((key, value) -> logger.info("{} -> {}", key, value));
		logger.info("===========================================");
	}

}
