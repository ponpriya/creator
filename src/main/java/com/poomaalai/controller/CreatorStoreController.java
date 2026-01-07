package com.poomaalai.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
@CrossOrigin(origins = "https://www.poomaalai.com",allowedHeaders={"Content-Type", "Authorization"},allowCredentials="true")  
@RequestMapping("/creator-store")
public class CreatorStoreController {

    private static final Logger logger = LoggerFactory.getLogger(CreatorStoreController.class);

    @Autowired
    private CreatorStoreService creatorStoreService;

    @Autowired
    private CreatorService creatorService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @GetMapping("/search")
    public ResponseEntity<List<CreatorStoreDto>> searchResults(@RequestParam(value = "zipcode", required = false) String zipcode) {
        List<CreatorStoreDto> creatorStoreDtos;   
        if(zipcode == null || zipcode.isEmpty()) {
            creatorStoreDtos = creatorStoreService.getAllCreatorStores();
            return ResponseEntity.status(HttpStatus.OK).body(creatorStoreDtos);
        }
        creatorStoreDtos = creatorStoreService.searchByZipcode(zipcode);
        logger.info("Found {} stores for zipcode: {}", creatorStoreDtos.size(), zipcode); 
        if (creatorStoreDtos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(creatorStoreDtos);
        }
        return ResponseEntity.status(HttpStatus.OK).body(creatorStoreDtos);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addStore(@Valid @RequestBody CreatorStoreDto creatorStoreDto, @RequestHeader(value = "Authorization", required = false) String authHeader) {

      logger.info("Adding new Creator Store: {}", creatorStoreDto.getName());          
      
      // Extract and validate Bearer token
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
          String token = authHeader.substring(7);
          if (jwtTokenProvider.validateToken(token)) {
              String email = jwtTokenProvider.getEmailFromToken(token);
              try {
                  UserDetails userDetails = (UserDetails) creatorService.loadUserByUsername(email);
                  UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                          userDetails, null, userDetails.getAuthorities());
                  SecurityContextHolder.getContext().setAuthentication(auth);
              } catch (UsernameNotFoundException ex) {
                  logger.warn("Could not set security context from token: {}", ex.getMessage());
              }
          } else {
              return ResponseEntity.status(401).body("Unauthorized: Invalid or expired token.");
          }
      }

      // Get authenticated user
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      logger.debug("Authenticated user: {}", auth != null ? auth.getName() : "none");
      if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
          return ResponseEntity.status(403).body("Unauthorized: User not authenticated.");
      }
      
      String email = auth.getName();
      Creator owner = creatorService.getCreatorByEmail(email);
      if (owner == null) {
          return ResponseEntity.status(403).body("Unauthorized: Creator not found.");
      }
      
      // Set the owner on the creatorStoreDto before saving
      creatorStoreDto.setCreatedBy(owner.getEmail());
      creatorStoreDto.setOwner(owner);
      creatorStoreService.addCreatorStore(creatorStoreDto);
      return ResponseEntity.status(201).body("Creator Store added successfully.");
    }

}
