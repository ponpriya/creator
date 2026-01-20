package com.poomaalai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterCreatorDto {

    
    @NotBlank
     @Pattern(
            regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M}\\s'\\-]{2,14}$",
            message = "First name must be between 3 to 15 characters and can contain letters (including international), spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank
     @Pattern(
            regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M}\\s'\\-]{2,14}$",
            message = "Last name must be between 3 to 15 characters and can contain letters (including international), spaces, hyphens, and apostrophes")
    private String lastName;

    @NotBlank
    @Size(min = 7, max = 20)
    @Pattern(regexp = "^[+]?[0-9\\-\\(\\)\\s]{7,20}$", message = "Invalid phone number")
    private String phone;

    @NotBlank
    @Size(min = 5, max = 500)
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s.,'\\-#/()]+$", message = "Invalid address")
    private String address;

    @NotBlank
    @Size(min = 3, max = 10)
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,10}$", message = "Invalid zipcode")
    private String zipcode;

    @NotBlank
    @Email
    private String email;


    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    
    @NotBlank
    @Size(min = 8, message = "Confirm Password must be at least 8 characters")  
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Confirm Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String confirmPassword;
    
}