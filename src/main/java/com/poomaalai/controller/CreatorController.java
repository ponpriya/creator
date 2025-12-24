package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poomaalai.dto.CreatorDto;
import com.poomaalai.dto.RegisterCreatorDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.service.CreatorService;

@RestController
@CrossOrigin(origins = "https://poomaalai-8b5b97a1-production.up.railway.app:8080", allowCredentials = "true")
@RequestMapping("/creator")
public class CreatorController {

    @Autowired
    private CreatorService creatorService;

    @GetMapping("/{id}")
    public ResponseEntity<CreatorDto> getCreatorById(@PathVariable int id) {   
        
        CreatorDto creatorDto = creatorService.getCreatorById(id);
        return ResponseEntity.status(HttpStatus.OK).body(creatorDto);
        
    }
    @GetMapping
    public List<CreatorDto> fetchAllCreators(){
        return creatorService.getAllCreators();
    }

    @GetMapping("/register")
    public String showRegistrationForm(RegisterCreatorDto registerCreatorDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("registerCreatorDto", new RegisterCreatorDto());
            model.addAttribute("registrationSuccess", false);
            return "register";
        }else{
            return "redirect:/creator/dashboard";

        } 
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterCreatorDto> registerCreator(@RequestBody RegisterCreatorDto registerCreatorDto) {
        System.out.println("Registering new creator : " + registerCreatorDto);
        if (creatorService.getCreatorByEmail(registerCreatorDto.getEmail()) != null) {
            System.out.println("Email already exists: " + registerCreatorDto.getEmail());
           // redirectAttributes.addFlashAttribute("alreadyRegistered", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
  /** 
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationSuccess", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
        **/
        // Normalize and validate email uniqueness
        String email = registerCreatorDto.getEmail() == null ? null : registerCreatorDto.getEmail().trim().toLowerCase();
        registerCreatorDto.setEmail(email);

        if (email != null && creatorService.getCreatorByEmail(email) != null) {
             //model.addAttribute("error", "emailexists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }

        if (!registerCreatorDto.getPassword().equals(registerCreatorDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerCreatorDto);
        }
        registerCreatorDto.setCreatedBy(email);
        System.out.println("Registering new creator : " + registerCreatorDto);
        creatorService.registerNewCreator(registerCreatorDto);
        if (creatorService.getCreatorByEmail(email) != null) {
            System.out.println("Creator registered successfully with email: " + email);
            //model.addAttribute("registrationSuccess", true);
            return ResponseEntity.ok(registerCreatorDto);
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerCreatorDto);
        }

    }
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
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

