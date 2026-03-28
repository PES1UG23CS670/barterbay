package com.barterbay.barterbay.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ✅ TEST ENDPOINT
    @GetMapping("/test")
    public String test() {
        return "Backend working perfectly ✅";
    }

    // ✅ SIGNUP WITH ERROR HANDLING
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestParam String username,
                                   @RequestParam String password) {

        try {
            User user = service.register(username, password);
            return ResponseEntity.status(201).body(user);

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}