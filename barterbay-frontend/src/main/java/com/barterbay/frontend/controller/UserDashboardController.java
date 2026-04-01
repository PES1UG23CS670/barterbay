package com.barterbay.frontend.controller;

import java.io.IOException;

import com.barterbay.frontend.model.UserSession;
import com.barterbay.frontend.service.NavigationService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    private final SessionManager sessionManager = ServiceRegistry.sessionManager();
    private final NavigationService navigationService = ServiceRegistry.navigationService();

    @FXML
    public void initialize() {
        UserSession currentUser = sessionManager.getCurrentUser();
        String username = currentUser == null ? "User" : currentUser.username();
        welcomeLabel.setText("Welcome, " + username);

        if (currentUser != null) {
            statusLabel.setText("Role: " + currentUser.role() + " | Status: " + currentUser.status());
        }
    }

    @FXML
    public void logout() {
        sessionManager.clear();
        try {
            navigationService.openLogin();
        } catch (IOException e) {
            statusLabel.setText("Unable to return to login page.");
        }
    }
}
