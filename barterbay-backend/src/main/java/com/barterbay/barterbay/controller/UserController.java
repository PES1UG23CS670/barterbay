package com.barterbay.barterbay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Object> signup(@RequestParam String username,
                                   @RequestParam String password) {

        try {
            User user = service.register(username, password);
            return ResponseEntity.status(201).body(user);

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    // ✅ LOGIN API
@PostMapping("/login")
public ResponseEntity<Object> login(@RequestParam String username,
                               @RequestParam String password) {

    try {
        User user = service.login(username, password);

        return ResponseEntity.ok(user);

    } catch (RuntimeException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }
}
}