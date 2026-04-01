package com.barterbay.frontend.controller;

import java.io.IOException;

import com.barterbay.frontend.model.UserSession;
import com.barterbay.frontend.service.AuthGateway;
import com.barterbay.frontend.service.NavigationService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private static final String STATUS_ERROR = "status-error";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final AuthGateway authGateway = ServiceRegistry.authGateway();
    private final NavigationService navigationService = ServiceRegistry.navigationService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            setError("Please enter username and password.");
            return;
        }

        try {
            UserSession session = authGateway.login(username, password);
            sessionManager.setCurrentUser(session);

            if (sessionManager.isAdmin()) {
                navigationService.openAdminDashboard();
                return;
            }

            navigationService.openUserDashboard();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            setError("Login failed: " + e.getMessage().replace('"', ' '));
        }
    }

    @FXML
    public void goToSignup() {
        try {
            navigationService.openSignup();
        } catch (IOException e) {
            setError("Unable to open signup page.");
        }
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success");
        if (!statusLabel.getStyleClass().contains(STATUS_ERROR)) {
            statusLabel.getStyleClass().add(STATUS_ERROR);
        }
    }
}
