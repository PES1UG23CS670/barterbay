package com.barterbay.barterbay.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barterbay.barterbay.dto.AdminExchangeSummaryDTO;
import com.barterbay.barterbay.dto.UserDetailsDTO;
import com.barterbay.barterbay.exception.BadRequestException;
import com.barterbay.barterbay.exception.NotFoundException;
import com.barterbay.barterbay.model.ExchangeRequest;
import com.barterbay.barterbay.model.Report;
import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.ExchangeRequestRepository;
import com.barterbay.barterbay.repository.ReportRepository;
import com.barterbay.barterbay.repository.UserRepository;

@Service
public class AdminService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final int TRUSTED_CREDIBILITY_THRESHOLD = 8;

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserSanitizer userSanitizer;

    public AdminService(
            UserRepository userRepository,
            ReportRepository reportRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            UserSanitizer userSanitizer) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.userSanitizer = userSanitizer;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userSanitizer::toSafeUser)
                .toList();
    }

    public void updateUserStatus(String id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        user.setStatus(status);
        userRepository.save(user);
    }

    public User getUserById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return userSanitizer.toSafeUser(user);
    }

    public UserDetailsDTO getUserDetails(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        List<Report> reports = reportRepository.findByReportedUser(id);
        int reportsCount = reports.size();
        int credibility = calculateCredibility(user, reportsCount);
        boolean suspicious = isSuspicious(credibility, reportsCount);
        boolean trusted = isTrusted(credibility, reportsCount);

        if (user.getCredibilityScore() != credibility) {
            user.setCredibilityScore(credibility);
            userRepository.save(user);
        }

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
        details.setTrusted(trusted);
        details.setTrustLevel(resolveTrustLevel(suspicious, trusted));
        details.setReports(new ArrayList<>(reports));
        details.setExchanges(Collections.emptyList());
        return details;
    }

    public void updateRating(String id, double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new BadRequestException("Rating must be between 0 and 5");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        int completedTrades = user.getTotalTrades();
        double updatedRating;
        if (completedTrades <= 0) {
            updatedRating = newRating;
        } else {
            updatedRating = ((user.getRating() * (completedTrades - 1)) + newRating) / completedTrades;
        }

        user.setRating(updatedRating);

        int reportsCount = reportRepository.findByReportedUser(id).size();
        user.setCredibilityScore(calculateCredibility(user, reportsCount));

        userRepository.save(user);
    }

    public UserDetailsDTO completeTrade(String id, double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new BadRequestException("Rating must be between 0 and 5");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.setTotalTrades(user.getTotalTrades() + 1);

        int completedTrades = user.getTotalTrades();
        double updatedRating = ((user.getRating() * (completedTrades - 1)) + newRating) / completedTrades;
        user.setRating(updatedRating);

        int reportsCount = reportRepository.findByReportedUser(id).size();
        int credibility = calculateCredibility(user, reportsCount);
        user.setCredibilityScore(credibility);

        userRepository.save(user);

        return buildUserDetails(user, reportsCount, credibility);
    }

    public void incrementTrades(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.setTotalTrades(user.getTotalTrades() + 1);

        int reportsCount = reportRepository.findByReportedUser(userId).size();
        user.setCredibilityScore(calculateCredibility(user, reportsCount));

        userRepository.save(user);
    }

    public List<AdminExchangeSummaryDTO> getAllExchanges() {
        return exchangeRequestRepository.findAll().stream()
                .map(this::toExchangeSummary)
                .toList();
    }

    public int calculateCredibility(User user, int reportsCount) {
        return (user.getTotalTrades() * 2)
                + (int) Math.round(user.getRating() * 2)
                - (reportsCount * 3);
    }

    public boolean isSuspicious(int credibility, int reportsCount) {
        return credibility < 0 || reportsCount > 3;
    }

    public boolean isTrusted(int credibility, int reportsCount) {
        return credibility >= TRUSTED_CREDIBILITY_THRESHOLD && reportsCount <= 1;
    }

    private UserDetailsDTO buildUserDetails(User user, int reportsCount, int credibility) {
        UserDetailsDTO details = new UserDetailsDTO();
        details.setId(user.getId());
        details.setUsername(user.getUsername());
        details.setRole(user.getRole());
        details.setStatus(user.getStatus());
        details.setCredibilityScore(credibility);
        details.setRating(user.getRating());
        details.setTotalTrades(user.getTotalTrades());
        details.setReportsCount(reportsCount);
        boolean suspicious = isSuspicious(credibility, reportsCount);
        boolean trusted = isTrusted(credibility, reportsCount);
        details.setSuspicious(suspicious);
        details.setTrusted(trusted);
        details.setTrustLevel(resolveTrustLevel(suspicious, trusted));
        details.setReports(new ArrayList<>(reportRepository.findByReportedUser(user.getId())));
        details.setExchanges(Collections.emptyList());
        return details;
    }

    private String resolveTrustLevel(boolean suspicious, boolean trusted) {
        if (suspicious) {
            return "SUSPICIOUS";
        }
        if (trusted) {
            return "TRUSTED";
        }
        return "NORMAL";
    }

    private AdminExchangeSummaryDTO toExchangeSummary(ExchangeRequest exchangeRequest) {
        AdminExchangeSummaryDTO summary = new AdminExchangeSummaryDTO();
        summary.setRequester(exchangeRequest.getRequesterId());
        summary.setReceiver(exchangeRequest.getReceiverId());
        summary.setStatus(exchangeRequest.getStatus());
        return summary;
    }
}
