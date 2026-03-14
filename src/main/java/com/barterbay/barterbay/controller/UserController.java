package com.barterbay.barterbay.controller;

import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserRepository repo;

    /* REGISTER USER */

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        User existingUser = repo.findByUsername(user.getUsername());

        if (existingUser != null) {
            return "Username already exists. Please choose another.";
        }

        repo.save(user);

        return "Account created successfully";
    }

    /* LOGIN USER */

    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User dbUser = repo.findByUsername(user.getUsername());

        if (dbUser == null) {
            return "User not found";
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            return "Wrong password";
        }

        return "Login successful";
    }
}