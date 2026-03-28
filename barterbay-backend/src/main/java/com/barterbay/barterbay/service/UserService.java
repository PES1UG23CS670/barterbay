package com.barterbay.barterbay.service;

import org.springframework.stereotype.Service;

import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // ✅ REGISTER USER WITH DUPLICATE CHECK
    public User register(String username, String password) {

        // 🔥 CHECK IF USER EXISTS
        if (repository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCredibilityScore(0);
        user.setPoints(10);
        user.setRating(0);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setTotalTrades(0);

        return repository.save(user);
    }
}