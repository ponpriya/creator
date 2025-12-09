package com.poomaalai.dto;
import com.poomaalai.entity.Creator;

import lombok.Data;

@Data
public class CreatorStoreDto {

    private int id;
    private String name;
    private String address;  
    private String phone;
    private String zipcode;
    private Creator owner;
    private String instagramHandle;
    private String facebookHandle;
    private String youtubeHandle;
}