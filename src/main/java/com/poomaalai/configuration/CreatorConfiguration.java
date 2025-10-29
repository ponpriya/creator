package com.poomaalai.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.poomaalai.service.CreatorService;
import org.modelmapper.ModelMapper;

@Configuration
public class CreatorConfiguration {


     @Bean
    public CreatorService creatorBean() {
        return new CreatorService();
    }

    @Bean
    public ModelMapper modelMapperBean() {
        return new ModelMapper();
    }
    
}
