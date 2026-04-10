package com.barterbay.frontend.controller;

import java.io.IOException;

import com.barterbay.frontend.model.UserSession;
import com.barterbay.frontend.service.NavigationService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML
    private StackPane contentArea;

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

        showBrowse();
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

    @FXML
    private void showBrowse() {
        loadPage("/view/browse.fxml");
    }

    @FXML
    private void showListings() {
        loadPage("/view/listings.fxml");
    }

    @FXML
    private void showExchanges() {
        loadPage("/view/exchanges.fxml");
    }

    private void loadPage(String path) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(path));
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
