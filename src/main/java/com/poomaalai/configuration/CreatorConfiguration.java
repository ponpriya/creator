package com.poomaalai.configuration;

import javax.sql.DataSource;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class CreatorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CreatorConfiguration.class);

    @Autowired
    private Environment env;

    @Autowired
    private DataSource dataSource;

/** 

     @Bean
    public CreatorService creatorBean() {
        return new CreatorService();
    }
**/
    @Bean
    public ModelMapper modelMapperBean() {
        return new ModelMapper();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CommandLineRunner logDatasourceInfo() {
        return args -> {
            logger.info("========== Datasource Configuration ==========");
            logger.info("Datasource URL: {}", env.getProperty("spring.datasource.url"));
           // logger.info("Datasource Username: {}", env.getProperty("spring.datasource.username"));
            logger.info("Datasource Driver: {}", env.getProperty("spring.datasource.driver-class-name"));
            logger.info("Datasource Connection: {}", dataSource.getConnection().getMetaData().getURL());
            logger.info("========== Liquibase Configuration ==========");
            logger.info("Liquibase Enabled: {}", env.getProperty("spring.liquibase.enabled"));
            logger.info("Liquibase ChangeLog: {}", env.getProperty("spring.liquibase.change-log"));
            logger.info("Liquibase Default Schema: {}", env.getProperty("spring.liquibase.default-schema"));
            logger.info("Liquibase Datasource URL: {}", env.getProperty("liquibase.command.url"));
            //logger.info("Liquibase Datasource Username: {}", env.getProperty("liquibase.command.username"));
           
            logger.info("==============================================");
        };
    }
    
}
