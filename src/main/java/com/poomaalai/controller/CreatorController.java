package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
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

@RestController
@CrossOrigin(origins = "http://localhost:8080",allowedHeaders="*",allowCredentials="true")
@RequestMapping("/creator")
public class CreatorController {

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
    public ResponseEntity<RegisterCreatorDto> registerCreator(@RequestBody RegisterCreatorDto registerCreatorDto) {
        System.out.println("Registering new creator : " + registerCreatorDto);
        if (creatorService.getCreatorByEmail(registerCreatorDto.getEmail()) != null) {
            System.out.println("Email already exists: " + registerCreatorDto.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
       // Normalize and validate email uniqueness
        String email = registerCreatorDto.getEmail() == null ? null : registerCreatorDto.getEmail().trim().toLowerCase();
        registerCreatorDto.setEmail(email);

        if (!registerCreatorDto.getPassword().equals(registerCreatorDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
        registerCreatorDto.setCreatedBy(email);
        System.out.println("Registering new creator : " + registerCreatorDto);
        creatorService.registerNewCreator(registerCreatorDto);
        if (creatorService.getCreatorByEmail(email) != null) {
            System.out.println("Creator registered successfully with email: " + email);
            return ResponseEntity.ok(registerCreatorDto);
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerCreatorDto);
        }

    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginCreatorDto loginDto) {
        System.out.println("Login attempt for email: " + loginDto.getEmail());

        // Normalize email
        String email = loginDto.getEmail() == null ? null : loginDto.getEmail().trim().toLowerCase();
        
        // Validate input
        if (email == null || email.isEmpty() || loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required.");
        }

        // Find creator by email
        Creator creator = creatorService.getCreatorByEmail(email);
        if (creator == null) {
            System.out.println("Creator not found with email: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        // Verify password
        if (!passwordEncoder.matches(loginDto.getPassword(), creator.getPassword())) {
            System.out.println("Invalid password for email: " + email);
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
            System.out.println("Security context set for: " + email);
            System.out.println("Authentication details: " + auth);
            System.out.println("Authentication in SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
        } catch (UsernameNotFoundException ex) {
            System.out.println("Warning: could not set security context: " + ex.getMessage());
        }

        System.out.println("Login successful for: " + email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/creator/login";
        }
        String email = auth.getName();
        Creator owner = creatorService.getCreatorByEmail(email);
        if (owner == null) {
            model.addAttribute("creatorStoreDtos", List.of());
            return "dashboard";
        }
        System.out.println("Logged in creator: " + owner.getEmail());
        System.out.println("Logged in creator email: " + email);
        model.addAttribute("creator", owner);
        model.addAttribute("creatorStoreDtos", creatorService.getAllCreatorStoresByOwnerEmail(email));
        return "dashboard";
    }
}

