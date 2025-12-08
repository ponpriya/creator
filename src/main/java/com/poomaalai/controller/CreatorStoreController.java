package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.service.CreatorService;
import com.poomaalai.service.CreatorStoreService;


@Controller
@RequestMapping("/creator-store")
public class CreatorStoreController {


    @Autowired
    private CreatorStoreService creatorStoreService;

    @Autowired
    private CreatorService creatorService;
    
    
    @GetMapping("/search")
    public String searchResults(@RequestParam(value = "zipcode", required = false) String zipcode, Model model) {
        if(zipcode == null || zipcode.isEmpty()) {
            return "search";
        }
        List<CreatorStoreDto> creatorStoreDtos = creatorStoreService.searchByZipcode(zipcode);
        model.addAttribute("creatorStoreDtos", creatorStoreDtos);
        return "search";
    }

    @GetMapping("/add")
    public String addStore(Model model) {
        model.addAttribute("creatorStore", new CreatorStoreDto());
        return "addStore";
    }
    @PostMapping("/add")
    public String addStore(@ModelAttribute("creatorStore") CreatorStoreDto creatorStoreDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/creator/login";
        }
        String email = auth.getName();
        Creator owner = creatorService.getCreatorByEmail(email);
        if (owner == null) {
            return "redirect:/creator/login";
        }
        
        // Set the owner on the creatorStoreDto before saving
        creatorStoreDto.setOwner(owner);
        creatorStoreService.addCreatorStore(creatorStoreDto);
        return "redirect:/creator/dashboard";
    }

}
