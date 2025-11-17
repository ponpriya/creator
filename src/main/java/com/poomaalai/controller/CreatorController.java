package com.poomaalai.controller;

import com.poomaalai.dto.CreatorResponse;
import com.poomaalai.service.CreatorService;

import ch.qos.logback.core.model.Model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;    


@RestController
@RequestMapping("/creators")
public class CreatorController {



    @Autowired
    private CreatorService creatorService;

    @GetMapping("/{id}")
    public ResponseEntity<CreatorResponse> getCreatorById(@PathVariable int id) {   
        
        CreatorResponse creator = creatorService.getCreatorById(id);
        return ResponseEntity.status(HttpStatus.OK).body(creator);
        
    }
    @GetMapping
    public List<CreatorResponse> fetchAllProducts(){
        return creatorService.getAllCreators();
    }
/** 
    @GetMapping("/search")
    public List<CreatorResponse> searchCreatorsByZipcode(@RequestParam("zipcode") String zipcode,Model model){
        return creatorService.searchCreatorsByZipcode(zipcode);
    }
**/
}

