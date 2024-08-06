package com.example.client.management.service;

import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.client.management.configuration.TokenService;
import com.example.client.management.dto.AuthenticationDto;
import com.example.client.management.dto.RegisterDto;
import com.example.client.management.models.UserModel;
import com.example.client.management.repository.UserRepository;

import jakarta.validation.Valid;

@Service
public class AuthorizationService implements UserDetailsService {
    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    public ResponseEntity<Object> login(@RequestBody @Valid AuthenticationDto data) {
        if (data.email() == null || data.email().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Email is empty.");
        }
        if (data.password() == null || data.password().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: password is empty.");
        }

        UserDetails user = userRepository.findByEmail(data.email());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Email does not exist.");
        }

        try {
            authenticationManager = context.getBean(AuthenticationManager.class);
            var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            var auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            var token = tokenService.generateToken((UserModel) auth.getPrincipal());
            return ResponseEntity.ok(token);
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: password is wrong.");
        }
    }



    public ResponseEntity<Object> register (@RequestBody RegisterDto registerDto, @RequestHeader("Authorization") String token) {
    	
        String userEmail = tokenService.validateToken(token.replace("Bearer ", ""));
        if (userEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        if (this.userRepository.findByEmail(registerDto.email()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(registerDto.password());
        UserModel newUser = new UserModel(registerDto.email(), encryptedPassword, registerDto.role());
        newUser.setCreatedAt(new Date(System.currentTimeMillis()));
        this.userRepository.save(newUser);
        return ResponseEntity.ok().build();
    }
    
    public ResponseEntity<Object> noTokenRegister(@RequestBody RegisterDto registerDto) {

        if (this.userRepository.findByEmail(registerDto.email()) != null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User already exists"));
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(registerDto.password());
        UserModel newUser = new UserModel(registerDto.email(), encryptedPassword, registerDto.role());
        newUser.setCreatedAt(new Date(System.currentTimeMillis()));
        this.userRepository.save(newUser);
        return ResponseEntity.ok().build();
    }

}