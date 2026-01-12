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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {"http://localhost:8080","http://localhost:8081","https://www.poomaalai.com","https://creator-production-8455.up.railway.app","https://poomaalai-8b5b97a1-production.up.railway.app","https://api.poomaalai.com"},allowedHeaders={"Content-Type", "Authorization"},allowCredentials="true")
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
    public ResponseEntity<RegisterCreatorDto> registerCreator(@Valid@RequestBody RegisterCreatorDto registerCreatorDto, HttpServletRequest request) {
        logger.info("Registration attempt for email");
        // Normalize email first
        String email = registerCreatorDto.getEmail() == null ? null : registerCreatorDto.getEmail().trim().toLowerCase();
        registerCreatorDto.setEmail(email);

        if (creatorService.getCreatorByEmail(email) != null) {
            logger.warn("Registration failed: Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }

        if (!registerCreatorDto.getPassword().equals(registerCreatorDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
        
        // Get client IP address
        String clientIp = getClientIpAddress(request);
        logger.info("Creating new creator with email from IP: {}", clientIp);
        creatorService.registerNewCreator(registerCreatorDto, clientIp);
        if (creatorService.getCreatorByEmail(email) != null) {
            logger.info("Creator registered successfully with email");
            registerCreatorDto.setPassword(null);
            registerCreatorDto.setConfirmPassword(null);    
            return ResponseEntity.ok(registerCreatorDto);
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerCreatorDto);
        }

    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginCreatorDto loginDto) {
        logger.info("Login attempt for email");

        // Normalize email
        String email = loginDto.getEmail() == null ? null : loginDto.getEmail().trim().toLowerCase();
        
        // Validate input
        if (email == null || email.isEmpty() || loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required.");
        }

        // Find creator by email
        Creator creator = creatorService.getCreatorByEmail(email);
        if (creator == null) {
            logger.warn("Login failed: Creator not found with email");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        // Verify password
        if (!passwordEncoder.matches(loginDto.getPassword(), creator.getPassword())) {
            logger.warn("Login failed: Invalid password for email");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(email);
        long expiresIn = jwtTokenProvider.getExpiryDuration();
        LoginResponseDto response = new LoginResponseDto(
            token,
            creator.getEmail(),
            creator.getFirstName() + " " + creator.getLastName(),
            expiresIn
        );

        // Set authentication in SecurityContext for this request
        try {
            UserDetails userDetails = (UserDetails) creatorService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            logger.debug("Security context set ");
        } catch (UsernameNotFoundException ex) {
            logger.warn("Could not set security context: {}", ex.getMessage());
        }

        logger.info("Login successful");
        return ResponseEntity.ok(response);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String ip = xForwardedFor.split(",")[0].trim();
            logger.info("IP from X-Forwarded-For: {}", ip);
            return normalizeLocalhost(ip);
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            logger.info("IP from X-Real-IP: {}", xRealIp);
            return normalizeLocalhost(xRealIp);
        }
        String remoteAddr = request.getRemoteAddr();
        logger.info("IP from RemoteAddr: {}", remoteAddr);
        return normalizeLocalhost(remoteAddr);
    }
    
    private String normalizeLocalhost(String ip) {
        if (ip == null) return "127.0.0.1";
        // Normalize IPv6 localhost to IPv4
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1";
        }
        return ip;
    }
}

