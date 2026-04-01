package com.barterbay.barterbay.service;

import org.springframework.stereotype.Service;

import com.barterbay.barterbay.exception.ForbiddenException;

@Service
public class RoleAuthorizationService {

    private static final String ADMIN_ROLE = "ADMIN";

    public void requireAdmin(String role) {
        if (!ADMIN_ROLE.equals(role)) {
            throw new ForbiddenException("Access denied");
        }
    }
}
