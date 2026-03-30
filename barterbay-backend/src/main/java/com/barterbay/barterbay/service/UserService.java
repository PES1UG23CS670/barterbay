package com.barterbay.barterbay.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barterbay.barterbay.dto.UserDetailsDTO;
import com.barterbay.barterbay.model.Report;
import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.ReportRepository;
import com.barterbay.barterbay.repository.UserRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository repository;
    private final ReportRepository reportRepository;

    public UserService(UserRepository repository, ReportRepository reportRepository) {
        this.repository = repository;
        this.reportRepository = reportRepository;
    }

    public List<User> getAllUsers() {
        return repository.findAll().stream()
                .map(this::maskPassword)
                .toList();
    }

    public User getUserById(String id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        return maskPassword(user);
    }

        public UserDetailsDTO getUserDetails(String id) {
        User user = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        List<Report> reports = reportRepository.findByReportedUser(user.getId());
        int reportsCount = reports.size();
        int credibility = (user.getTotalTrades() * 2) + ((int) user.getRating() * 2) - (reportsCount * 3);
        boolean suspicious = credibility < 0 || reportsCount > 3;

            UserDetailsDTO details = new UserDetailsDTO();
            details.setId(user.getId());
            details.setUsername(user.getUsername());
            details.setRole(user.getRole());
            details.setStatus(user.getStatus());
            details.setCredibilityScore(credibility);
            details.setRating(user.getRating());
            details.setTotalTrades(user.getTotalTrades());
            details.setReportsCount(reportsCount);
            details.setSuspicious(suspicious);
            details.setReports(new ArrayList<>(reports));
            details.setExchanges(Collections.emptyList());
            return details;
        }

    public void updateUserStatus(String id, String status) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        user.setStatus(status);
        repository.save(user);
    }

    private User maskPassword(User user) {
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

    // ✅ REGISTER USER WITH DUPLICATE CHECK
    public User register(String username, String password) {

        // 🔥 CHECK IF USER EXISTS
        if (repository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("User already exists");
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
    public User login(String username, String password) {

    User user = repository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    // 🔐 check password
    if (!user.getPassword().equals(password)) {
        throw new IllegalArgumentException("Invalid password");
    }

    // 🚫 check if suspended
    if (!user.getStatus().equals("ACTIVE")) {
        throw new IllegalStateException("User is suspended. Contact admin.");
    }

    return user;
}

}