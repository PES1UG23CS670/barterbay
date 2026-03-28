package com.barterbay.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.barterbay.frontend.service.ApiService;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final ApiService apiService = new ApiService();

    @FXML
    public void handleSignup() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean success = apiService.signup(username, password);

            if (success) {
                statusLabel.setText("Signup successful!");
            } else {
                statusLabel.setText("Signup failed");
            }

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}