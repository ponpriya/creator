package com.poomaalai.dto;

import lombok.Data;

@Data
public class RegisterCreatorDto {

    private String name;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String zipcode;
    private String email;
    private String password;
    private String confirmPassword;
    
}