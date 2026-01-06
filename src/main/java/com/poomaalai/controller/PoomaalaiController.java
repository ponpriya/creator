package com.poomaalai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class PoomaalaiController {

    @GetMapping("error")
    public ResponseEntity<String> errorPage() {
       return ResponseEntity.status(404).body("Error: Requested resource not found"); 
    }
}
