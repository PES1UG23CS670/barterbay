package com.barterbay.frontend.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserRow {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public UserRow(String id, String username, String role, String status) {
        this.id.set(id);
        this.username.set(username);
        this.role.set(role);
        this.status.set(status);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}
