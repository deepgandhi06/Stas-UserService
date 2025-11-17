package com.stas.user_service.controller;


import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.stas.user_service.DTO.UserDTO;
import com.stas.user_service.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO userDTO) {
        return ResponseEntity.status(201).body(authService.signup(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.login(userDTO));
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> update(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.update(userDTO));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody UserDTO userDTO) {
        authService.changePassword(userDTO);
        return ResponseEntity.ok("Password updated successfully");
    }
}
