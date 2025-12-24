package com.poomaalai.dto;

import com.poomaalai.entity.Auditable;
import com.poomaalai.entity.Creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatorStoreDto extends Auditable{

    private int id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String address;

    @NotBlank
    @Pattern(regexp = "[0-9\\-\\+\\(\\)\\s]{7,15}", message = "Invalid phone number")
    private String phone;

    @NotBlank
    @Pattern(regexp = "\\d{5}", message = "Zipcode must be 5 digits")
    private String zipcode;

    private Creator owner;

    @Size(max = 100)
    private String instagramHandle;

    @Size(max = 100)
    private String facebookHandle;

    @Size(max = 100)
    private String youtubeHandle;
}