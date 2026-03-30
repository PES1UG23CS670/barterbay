package com.barterbay.barterbay.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDetailsDTO {

    @JsonProperty("_id")
    private String id;
    private String username;
    private String role;
    private String status;
    private int credibilityScore;
    private double rating;
    private int totalTrades;
    private int reportsCount;
    private boolean suspicious;
    private List<Object> reports;
    private List<Object> exchanges;

    public UserDetailsDTO() {
        // Default constructor for serialization/deserialization.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCredibilityScore() {
        return credibilityScore;
    }

    public void setCredibilityScore(int credibilityScore) {
        this.credibilityScore = credibilityScore;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public void setReportsCount(int reportsCount) {
        this.reportsCount = reportsCount;
    }

    public boolean isSuspicious() {
        return suspicious;
    }

    public void setSuspicious(boolean suspicious) {
        this.suspicious = suspicious;
    }

    public List<Object> getReports() {
        return reports;
    }

    public void setReports(List<Object> reports) {
        this.reports = reports;
    }

    public List<Object> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<Object> exchanges) {
        this.exchanges = exchanges;
    }
}
