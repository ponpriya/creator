package com.poomaalai.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseDebugConfig implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseDebugConfig.class);

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private SpringLiquibase springLiquibase;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("========== Liquibase Debug Information ==========");
        
        if (springLiquibase == null) {
            logger.error("SpringLiquibase bean is NULL - Liquibase is NOT running!");
            logger.error("Check if spring.liquibase.enabled=true in active profile");
            logger.error("Active profiles: {}", String.join(", ", env.getActiveProfiles()));
            logger.error("spring.liquibase.enabled property: {}", env.getProperty("spring.liquibase.enabled"));
            logger.error("spring.liquibase.change-log property: {}", env.getProperty("spring.liquibase.change-log"));
        } else {
            logger.info("SpringLiquibase bean is present - Liquibase SHOULD have run");
            logger.info("ChangeLog: {}", springLiquibase.getChangeLog());
            logger.info("DataSource: {}", springLiquibase.getDataSource());
            logger.info("Default Schema: {}", springLiquibase.getDefaultSchema());
            logger.info("Contexts: {}", springLiquibase.getContexts());
        }
        
        logger.info("================================================");
    }
}
