package com.poomaalai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.poomaalai.entity.Auditable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterCreatorDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "Store name must be between 3 to 15 characters and can only contain letters, numbers, and underscores")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "First name must be between 3 to 15 characters and can only contain letters, numbers, and underscores")
    private String firstName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,15}$", message = "Last name must be between 3 to 15 characters and can only contain letters, numbers, and underscores")
    private String lastName;

    @NotBlank
    @Pattern(regexp = "[0-9\\-\\+\\(\\)\\s]{7,15}", message = "Invalid phone number")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[#.0-9a-zA-Z\\s,-]+$", message = "Address must be between 3 to 15 characters and can only contain letters, numbers, and underscores")
    private String address;

    @NotBlank
    @Pattern(regexp = "\\d{5}", message = "Zipcode must be 5 digits")
    private String zipcode;

    @NotBlank
    @Email
    private String email;

    @JsonIgnore
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @JsonIgnore
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String confirmPassword;
    
}