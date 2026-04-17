package com.barterbay.barterbay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barterbay.barterbay.service.AdminService;
import com.barterbay.barterbay.service.RoleAuthorizationService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final AdminService adminService;
    private final RoleAuthorizationService roleAuthorizationService;

    public AdminController(AdminService adminService, RoleAuthorizationService roleAuthorizationService) {
        this.adminService = adminService;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    // ✅ GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers(@RequestParam String role) {
        roleAuthorizationService.requireAdmin(role);

        return ResponseEntity.ok((Object) adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Object> updateStatus(@PathVariable String id,
            @RequestParam String status,
            @RequestParam String role) {
        roleAuthorizationService.requireAdmin(role);

        adminService.updateUserStatus(id, status);

        return ResponseEntity.ok((Object) "User status updated");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id,
            @RequestParam String role) {
        roleAuthorizationService.requireAdmin(role);

        return ResponseEntity.ok((Object) adminService.getUserById(id));
    }
    @GetMapping("/users/{id}/details")
    public ResponseEntity<Object> getUserDetails(@PathVariable String id,
            @RequestParam String role) {
        roleAuthorizationService.requireAdmin(role);

        return ResponseEntity.ok((Object) adminService.getUserDetails(id));
    }

    @GetMapping("/exchanges")
    public ResponseEntity<Object> getAllExchanges(@RequestParam(required = false) String role) {
        roleAuthorizationService.requireAdmin(role);

        return ResponseEntity.ok((Object) adminService.getAllExchanges());
    }
}