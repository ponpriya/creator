package com.poomaalai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.poomaalai.entity.Creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor  
@AllArgsConstructor
public class CreatorStoreDto{

    @NotBlank
    @Size(max = 200)
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\(\\)]{2,200}$", message = "Invalid name")
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s.,'\\-#/()]+$", message = "Invalid address")
    private String address;

    @NotBlank
    @Size(min = 7, max = 20)
    @Pattern(regexp = "^[+]?[0-9\\-\\(\\)\\s]{7,20}$", message = "Invalid phone number")
    private String phone;

    @NotBlank
    @Size(min = 3, max = 10)
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,10}$", message = "Invalid zipcode")
    private String zipcode;

    private Double latitude;

    private Double longitude;

    @JsonIgnore
    private Creator owner;

    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_\\.]{1,100}$", message = "Invalid Instagram handle") 
    private String instagramHandle;

    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_\\.]{1,100}$", message = "Invalid Facebook handle")  
    private String facebookHandle;

    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_\\.]{1,100}$", message = "Invalid YouTube handle")
    private String youtubeHandle;
}