package com.poomaalai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poomaalai.entity.Creator;
import com.poomaalai.service.CreatorService;

import org.springframework.ui.Model;


@Controller
@RequestMapping("/creator")
public class PoomaalaiController {


    @Autowired
    private CreatorService creatorService;
    
    
    @GetMapping("/search")
    public String searchResults(@RequestParam(value = "zipcode", required = false) String zipcode, Model model) {
        if(zipcode == null || zipcode.isEmpty()) {
            return "search";
        }
        List<Creator> creators = creatorService.searchCreatorsByZipcode(zipcode);
        model.addAttribute("creators", creators);
        return "search";
    }

}
