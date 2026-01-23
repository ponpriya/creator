package com.poomaalai.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.security.JwtTokenProvider;
import com.poomaalai.service.CreatorService;
import com.poomaalai.service.CreatorStoreService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/creator-store")
public class CreatorStoreController {

    private static final Logger logger = LoggerFactory.getLogger(CreatorStoreController.class);

    @Autowired
    private CreatorStoreService creatorStoreService;

    @Autowired
    private CreatorService creatorService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final java.util.regex.Pattern ZIPCODE_PATTERN = 
        java.util.regex.Pattern.compile("\\d{5}");
    
    @GetMapping("/search")
    public ResponseEntity<List<CreatorStoreDto>> searchResults(@RequestParam(value = "zipcode", required = false) String zipcode) {
        List<CreatorStoreDto> creatorStoreDtos;   
        if(zipcode == null || zipcode.isEmpty()) {
            creatorStoreDtos = creatorStoreService.getAllCreatorStores();
            return ResponseEntity.status(HttpStatus.OK).body(creatorStoreDtos);
        }
        if (!ZIPCODE_PATTERN.matcher(zipcode).matches()) {
           return ResponseEntity.badRequest().build();
       }
        creatorStoreDtos = creatorStoreService.searchByZipcode(zipcode);
        logger.info("Found {} stores for zipcode: {}", creatorStoreDtos.size(), zipcode); 
        return ResponseEntity.status(HttpStatus.OK).body(creatorStoreDtos);
    }

    @GetMapping("/search-radius")
    public ResponseEntity<List<CreatorStoreDto>> searchByRadius(
            @RequestParam(value = "zipcode") String zipcode,
            @RequestParam(value = "radius", defaultValue = "10") int radiusMiles) {
        
        if (!ZIPCODE_PATTERN.matcher(zipcode).matches()) {
            logger.warn("Invalid zipcode format: {}", zipcode);
            return ResponseEntity.badRequest().build();
        }
        
        if (radiusMiles < 0 || radiusMiles > 500) {
            logger.warn("Invalid radius value: {}", radiusMiles);
            return ResponseEntity.badRequest().build();
        }
        
        List<CreatorStoreDto> creatorStoreDtos = creatorStoreService.searchByZipcodeAndRadius(zipcode, radiusMiles);
        logger.info("Found {} stores within {} miles of zipcode: {}", 
                   creatorStoreDtos.size(), radiusMiles, zipcode);
        return ResponseEntity.status(HttpStatus.OK).body(creatorStoreDtos);
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addStore(@Valid @RequestBody CreatorStoreDto creatorStoreDto) {

      logger.info("Adding new Creator Store: {}", creatorStoreDto.getName());          
      
      // Get authenticated user
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      logger.debug("Authenticated user: {}", auth != null ? auth.getName() : "none");
      if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: User not authenticated.");
      }
      
      String email = auth.getName();
      Creator owner = creatorService.getCreatorByEmail(email);
      if (owner == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Creator not found.");
      }
      
      // Set the owner on the creatorStoreDto before saving
      creatorStoreDto.setOwner(owner);
      creatorStoreService.addCreatorStore(creatorStoreDto);
      return ResponseEntity.status(201).body("Creator Store added successfully.");
    }

}
