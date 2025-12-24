package com.poomaalai.dto;

import com.poomaalai.entity.Auditable;

import lombok.Data;

@Data
public class LoginCreatorDto  extends Auditable{

    private String email;
    private String password;
    
}