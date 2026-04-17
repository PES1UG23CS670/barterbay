package com.barterbay.barterbay.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "users")
public class User {

    @Id
    @JsonProperty("_id")
    private String id;

    private String username;
    private String password;

    private int credibilityScore;
    private int points;
    private double rating;

    private String role;
    private String status;
    private int totalTrades;

    public User() {
        /* Default constructor for Spring Data mapping. */
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCredibilityScore() {
        return credibilityScore;
    }

    public void setCredibilityScore(int credibilityScore) {
        this.credibilityScore = credibilityScore;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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

    public int getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }
}