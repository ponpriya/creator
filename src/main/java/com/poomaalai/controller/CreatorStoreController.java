package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.service.CreatorStoreService;


@Controller
@RequestMapping("/creator-store")
public class CreatorStoreController {


    @Autowired
    private CreatorStoreService creatorStoreService;
    
    
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
    public String addStore(@ModelAttribute("creatorStore") CreatorStoreDto creatorStoreDto) {
        creatorStoreService.addCreatorStore(creatorStoreDto);
        return "redirect:/creator-store/add?success";
    }

}
