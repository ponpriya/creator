package com.poomaalai.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreatorConfiguration {

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
    
}
