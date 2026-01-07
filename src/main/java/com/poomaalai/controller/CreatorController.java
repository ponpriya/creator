package com.poomaalai.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poomaalai.dto.CreatorDto;
import com.poomaalai.dto.LoginCreatorDto;
import com.poomaalai.dto.LoginResponseDto;
import com.poomaalai.dto.RegisterCreatorDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.security.JwtTokenProvider;
import com.poomaalai.service.CreatorService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {"http://localhost:8080","http://localhost:8081","https://www.poomaalai.com","https://creator-production-8455.up.railway.app"},allowedHeaders={"Content-Type", "Authorization"},allowCredentials="true")
@RequestMapping("/creator")
public class CreatorController {

    private static final Logger logger = LoggerFactory.getLogger(CreatorController.class);

    @Autowired
    private CreatorService creatorService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<CreatorDto> getCreatorById(@PathVariable int id) {   
        
        CreatorDto creatorDto = creatorService.getCreatorById(id);
        return ResponseEntity.status(HttpStatus.OK).body(creatorDto);
        
    }
    @GetMapping
    public List<CreatorDto> fetchAllCreators(){
        return creatorService.getAllCreators();
    }
    @PostMapping("/api/register")
    public ResponseEntity<RegisterCreatorDto> registerCreator(@Valid @RequestBody RegisterCreatorDto registerCreatorDto) {
        logger.info("Registration attempt for email: {}", registerCreatorDto.getEmail());
        if (creatorService.getCreatorByEmail(registerCreatorDto.getEmail()) != null) {
            logger.warn("Registration failed: Email already exists: {}", registerCreatorDto.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
       // Normalize and validate email uniqueness
        String email = registerCreatorDto.getEmail() == null ? null : registerCreatorDto.getEmail().trim().toLowerCase();
        registerCreatorDto.setEmail(email);

        if (!registerCreatorDto.getPassword().equals(registerCreatorDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
        registerCreatorDto.setCreatedBy(email);
        logger.info("Creating new creator with email: {}", email);
        creatorService.registerNewCreator(registerCreatorDto);
        if (creatorService.getCreatorByEmail(email) != null) {
            logger.info("Creator registered successfully with email: {}", email);
            return ResponseEntity.ok(registerCreatorDto);
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerCreatorDto);
        }

    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginCreatorDto loginDto) {
        logger.info("Login attempt for email: {}", loginDto.getEmail());

        // Normalize email
        String email = loginDto.getEmail() == null ? null : loginDto.getEmail().trim().toLowerCase();
        
        // Validate input
        if (email == null || email.isEmpty() || loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required.");
        }

        // Find creator by email
        Creator creator = creatorService.getCreatorByEmail(email);
        if (creator == null) {
            logger.warn("Login failed: Creator not found with email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        // Verify password
        if (!passwordEncoder.matches(loginDto.getPassword(), creator.getPassword())) {
            logger.warn("Login failed: Invalid password for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(email);
        long expiresIn = 86400000; // 24 hours in milliseconds
        LoginResponseDto response = new LoginResponseDto(
            token,
            creator.getEmail(),
            creator.getName(),
            expiresIn
        );

        // Set authentication in SecurityContext for this request
        try {
            UserDetails userDetails = (UserDetails) creatorService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            logger.debug("Security context set for: {}", email);
        } catch (UsernameNotFoundException ex) {
            logger.warn("Could not set security context: {}", ex.getMessage());
        }

        logger.info("Login successful for: {}", email);
        return ResponseEntity.ok(response);
    }
    
}

