package com.barterbay.frontend.controller;

import java.io.IOException;

import com.barterbay.frontend.service.AuthGateway;
import com.barterbay.frontend.service.NavigationService;
import com.barterbay.frontend.service.ServiceRegistry;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    private static final String STATUS_ERROR = "status-error";
    private static final String STATUS_SUCCESS = "status-success";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final AuthGateway authGateway = ServiceRegistry.authGateway();
    private final NavigationService navigationService = ServiceRegistry.navigationService();

    @FXML
    public void handleSignup() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            setError("Please enter username and password.");
            return;
        }

        try {
            boolean success = authGateway.signup(username, password);

            if (success) {
                statusLabel.setText("Signup successful! You can login now.");
                statusLabel.getStyleClass().removeAll(STATUS_ERROR);
                if (!statusLabel.getStyleClass().contains(STATUS_SUCCESS)) {
                    statusLabel.getStyleClass().add(STATUS_SUCCESS);
                }
                usernameField.clear();
                passwordField.clear();
            } else {
                setError("Signup failed.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Signup interrupted.");
        } catch (IOException | RuntimeException e) {
            setError("Error: " + e.getMessage().replace('"', ' '));
        }
    }

    @FXML
    public void goToLogin() {
        try {
            navigationService.openLogin();
        } catch (IOException e) {
            setError("Unable to open login page.");
        }
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll(STATUS_SUCCESS);
        if (!statusLabel.getStyleClass().contains(STATUS_ERROR)) {
            statusLabel.getStyleClass().add(STATUS_ERROR);
        }
    }
}