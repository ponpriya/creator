package com.poomaalai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CreatorDto{

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private String address; 
    private String phone;
    private String zipcode;
    
}