package com.example.client.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.client.management.dto.AuthenticationDto;
import com.example.client.management.dto.RegisterDto;
import com.example.client.management.service.AuthorizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthController {
   
    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid AuthenticationDto authenticationDto){
        return authorizationService.login(authenticationDto);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register (@RequestBody RegisterDto registerDto, @RequestHeader("Authorization") String token) {
        return authorizationService.register(registerDto, token);
    }
    
    @PostMapping("/register/user")
    public ResponseEntity<Object> noTokenRegister (@RequestBody RegisterDto registerDto) {
        return authorizationService.noTokenRegister(registerDto);
    }
}