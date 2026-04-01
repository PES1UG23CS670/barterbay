package com.barterbay.barterbay.service;

import org.springframework.stereotype.Component;

import com.barterbay.barterbay.model.User;

@Component
public class UserSanitizer {

    public User toSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setPassword(null);
        safeUser.setCredibilityScore(user.getCredibilityScore());
        safeUser.setPoints(user.getPoints());
        safeUser.setRating(user.getRating());
        safeUser.setRole(user.getRole());
        safeUser.setStatus(user.getStatus());
        safeUser.setTotalTrades(user.getTotalTrades());
        return safeUser;
    }
}
