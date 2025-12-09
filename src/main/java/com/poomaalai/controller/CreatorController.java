package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poomaalai.dto.CreatorDto;
import com.poomaalai.dto.RegisterCreatorDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.service.CreatorService;


@Controller
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
    public String showRegistrationForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("creator", new RegisterCreatorDto());
            model.addAttribute("registrationSuccess", false);
            return "register";
        }
        return "redirect:/creator/dashboard";  
    }
    @PostMapping("/register")
    public String registerCreator(@ModelAttribute("creator") RegisterCreatorDto registerCreatorDto,Model model) {
        if (!registerCreatorDto.getPassword().equals(registerCreatorDto.getConfirmPassword())) {
            return "redirect:/creator/register?error=passwordmismatch";
        }
        creatorService.registerNewCreator(registerCreatorDto);
        model.addAttribute("registrationSuccess", true);
        return "register";
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

