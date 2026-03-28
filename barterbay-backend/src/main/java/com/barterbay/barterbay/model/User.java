package com.barterbay.barterbay.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String username;
    private String password;

    private int credibilityScore;
    private int points;
    private double rating;

    private String role;
    private String status;
    private int totalTrades;
}