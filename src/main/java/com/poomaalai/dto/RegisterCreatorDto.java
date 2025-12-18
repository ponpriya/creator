package com.poomaalai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterCreatorDto {

    @NotBlank
    private String name;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "[0-9\\-\\+\\(\\)\\s]{7,15}", message = "Invalid phone number")
    private String phone;

    @NotBlank
    private String address;

    @NotBlank
    @Pattern(regexp = "\\d{5}", message = "Zipcode must be 5 digits")
    private String zipcode;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String confirmPassword;
    
}