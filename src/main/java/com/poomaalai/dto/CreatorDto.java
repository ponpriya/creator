package com.poomaalai.dto;

import com.poomaalai.entity.Auditable;

import lombok.Data;

@Data
public class CreatorDto extends Auditable{

    private int id;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address; 
    private String phone;
    private String zipcode;
    
}