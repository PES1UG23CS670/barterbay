package com.barterbay.barterbay.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barterbay.barterbay.exception.BadRequestException;
import com.barterbay.barterbay.exception.NotFoundException;
import com.barterbay.barterbay.exception.UnauthorizedException;
import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.UserRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository repository;
    private final UserSanitizer userSanitizer;
    private final AdminService adminService;

    public UserService(UserRepository repository, UserSanitizer userSanitizer, AdminService adminService) {
        this.repository = repository;
        this.userSanitizer = userSanitizer;
        this.adminService = adminService;
    }

    public List<User> getAllUsers() {
        return repository.findAll().stream()
                .map(userSanitizer::toSafeUser)
                .toList();
    }

    public User getUserById(String id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        return userSanitizer.toSafeUser(user);
    }

    public void updateUserStatus(String id, String status) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.setStatus(status);
        repository.save(user);
    }

    public void updateRating(String id, double newRating) {
        adminService.updateRating(id, newRating);
    }

    // ✅ REGISTER USER WITH DUPLICATE CHECK
    public User register(String username, String password) {

        // 🔥 CHECK IF USER EXISTS
        if (repository.findByUsername(username).isPresent()) {
            throw new BadRequestException("User already exists");
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

        return userSanitizer.toSafeUser(repository.save(user));
    }
    public User login(String username, String password) {

    User user = repository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

    // 🔐 check password
    if (!user.getPassword().equals(password)) {
        throw new UnauthorizedException("Invalid password");
    }

    // 🚫 check if suspended
    if (!user.getStatus().equals("ACTIVE")) {
        throw new UnauthorizedException("User is suspended. Contact admin.");
    }

    return userSanitizer.toSafeUser(user);
}


}