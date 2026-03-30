package com.barterbay.barterbay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barterbay.barterbay.service.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ACCESS_DENIED = "Access denied";

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ✅ GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers(@RequestParam String role) {

        // 🔒 Basic protection
        if (!ADMIN_ROLE.equals(role)) {
            return ResponseEntity.status(403).body(ACCESS_DENIED);
        }

        return ResponseEntity.ok((Object) userService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Object> updateStatus(@PathVariable String id,
            @RequestParam String status,
            @RequestParam String role) {

        if (!ADMIN_ROLE.equals(role)) {
            return ResponseEntity.status(403).body(ACCESS_DENIED);
        }

        userService.updateUserStatus(id, status);

        return ResponseEntity.ok((Object) "User status updated");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id,
            @RequestParam String role) {

        if (!ADMIN_ROLE.equals(role)) {
            return ResponseEntity.status(403).body(ACCESS_DENIED);
        }

        return ResponseEntity.ok((Object) userService.getUserById(id));
    }
    @GetMapping("/users/{id}/details")
    public ResponseEntity<Object> getUserDetails(@PathVariable String id,
            @RequestParam String role) {
        if (!ADMIN_ROLE.equals(role)) {
            return ResponseEntity.status(403).body(ACCESS_DENIED);
        }

        return ResponseEntity.ok((Object) userService.getUserDetails(id));
    }
}