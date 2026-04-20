package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.barterbay.frontend.model.ExchangeRow;
import com.barterbay.frontend.model.UserRow;
import com.barterbay.frontend.service.AdminGateway;
import com.barterbay.frontend.service.NavigationService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;

public class AdminDashboardController {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String STATUS_SUCCESS = "status-success";
    private static final String STATUS_ERROR = "status-error";

    @FXML private Label headerLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> userIdCol;
    @FXML private TableColumn<UserRow, String> usernameCol;
    @FXML private TableColumn<UserRow, String> userRoleCol;
    @FXML private TableColumn<UserRow, String> userStatusCol;

    @FXML private TableView<ExchangeRow> exchangesTable;
    @FXML private TableColumn<ExchangeRow, String> requesterCol;
    @FXML private TableColumn<ExchangeRow, String> receiverCol;
    @FXML private TableColumn<ExchangeRow, String> exchangeStatusCol;

    @FXML private TextArea selectedUserDetailsArea;

    private final AdminGateway adminGateway = ServiceRegistry.adminGateway();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();
    private final NavigationService navigationService = ServiceRegistry.navigationService();

    @FXML
    public void initialize() {
        userIdCol.setCellValueFactory(data -> data.getValue().idProperty());
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        userRoleCol.setCellValueFactory(data -> data.getValue().roleProperty());
        userStatusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        requesterCol.setCellValueFactory(data -> data.getValue().requesterProperty());
        receiverCol.setCellValueFactory(data -> data.getValue().receiverProperty());
        exchangeStatusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        String username = sessionManager.getCurrentUser() == null ? "Admin" : sessionManager.getCurrentUser().username();
        headerLabel.setText("Welcome, " + username);

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSelectedUserDetails(newVal.getId());
            }
        });

        refreshAll();
    }

    @FXML
    public void refreshAll() {
        try {
            List<UserRow> allUsers = adminGateway.getAllUsers(ADMIN_ROLE);
            usersTable.setItems(FXCollections.observableArrayList(allUsers));

            List<ExchangeRow> allExchanges = adminGateway.getExchanges(ADMIN_ROLE);
            exchangesTable.setItems(FXCollections.observableArrayList(allExchanges));

            setSuccess("Admin data loaded.");
        } catch (Exception e) {
            setError("Failed to load dashboard: " + e.getMessage().replace('"', ' '));
        }
    }

    @FXML
    public void blockSelectedUser() {
        updateSelectedUserStatus("BLOCKED");
    }

    @FXML
    public void activateSelectedUser() {
        updateSelectedUserStatus("ACTIVE");
    }

    @FXML
    public void logout() {
        sessionManager.clear();
        try {
            navigationService.openLogin();
        } catch (IOException e) {
            setError("Unable to return to login page.");
        }
    }

    private void updateSelectedUserStatus(String status) {
        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setError("Please select a user first.");
            return;
        }

        try {
            adminGateway.updateUserStatus(selected.getId(), status, ADMIN_ROLE);
            refreshAll();
            setSuccess("Updated user status to " + status + ".");
        } catch (Exception e) {
            setError("Unable to update status: " + e.getMessage().replace('"', ' '));
        }
    }

    private void loadSelectedUserDetails(String userId) {
        try {
            JsonNode details = adminGateway.getUserDetails(userId, ADMIN_ROLE);
            String info = "User Details\n"
                    + "ID: " + text(details, "id", "_id") + "\n"
                    + "Username: " + text(details, "username") + "\n"
                    + "Role: " + text(details, "role") + "\n"
                    + "Status: " + text(details, "status") + "\n"
                    + "Credibility: " + text(details, "credibilityScore") + "\n"
                    + "Reports: " + text(details, "reportsCount") + "\n"
                    + "Trust: " + text(details, "trustLevel");
            selectedUserDetailsArea.setText(info);
        } catch (Exception e) {
            setError("Unable to load user details: " + e.getMessage().replace('"', ' '));
        }
    }

    private String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.get(key);
            if (child != null && !child.isNull()) {
                return child.asText();
            }
        }
        return "";
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll(STATUS_SUCCESS);
        if (!statusLabel.getStyleClass().contains(STATUS_ERROR)) {
            statusLabel.getStyleClass().add(STATUS_ERROR);
        }
    }

    private void setSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll(STATUS_ERROR);
        if (!statusLabel.getStyleClass().contains(STATUS_SUCCESS)) {
            statusLabel.getStyleClass().add(STATUS_SUCCESS);
        }
    }
}
