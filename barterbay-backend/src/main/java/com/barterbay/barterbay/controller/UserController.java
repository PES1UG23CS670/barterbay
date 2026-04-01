package com.barterbay.barterbay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barterbay.barterbay.exception.BadRequestException;
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
    public ResponseEntity<Object> signup(@RequestBody(required = false) AuthPayload payload,
                                         @RequestParam(required = false) String username,
                                         @RequestParam(required = false) String password) {

        String resolvedUsername = firstNonBlank(payload != null ? payload.getUsername() : null, username);
        String resolvedPassword = firstNonBlank(payload != null ? payload.getPassword() : null, password);

        if (resolvedUsername == null || resolvedPassword == null) {
            throw new BadRequestException("username and password are required");
        }

        User user = service.register(resolvedUsername, resolvedPassword);
        return ResponseEntity.status(201).body(user);
    }
    // ✅ LOGIN API
@PostMapping("/login")
public ResponseEntity<Object> login(@RequestBody(required = false) AuthPayload payload,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) String password) {

    String resolvedUsername = firstNonBlank(payload != null ? payload.getUsername() : null, username);
    String resolvedPassword = firstNonBlank(payload != null ? payload.getPassword() : null, password);

    if (resolvedUsername == null || resolvedPassword == null) {
        throw new BadRequestException("username and password are required");
    }

    User user = service.login(resolvedUsername, resolvedPassword);

    return ResponseEntity.ok(user);
}
@PutMapping("/rate/{id}")
public ResponseEntity<Object> rateUser(@PathVariable String id,
                                  @RequestParam double rating) {
    service.updateRating(id, rating);
    return ResponseEntity.ok((Object) "Rating updated");
}

private String firstNonBlank(String primary, String fallback) {
    if (primary != null && !primary.isBlank()) {
        return primary;
    }
    if (fallback != null && !fallback.isBlank()) {
        return fallback;
    }
    return null;
}

public static class AuthPayload {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
}