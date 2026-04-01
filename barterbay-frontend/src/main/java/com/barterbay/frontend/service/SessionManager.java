package com.barterbay.frontend.service;

import com.barterbay.frontend.model.UserSession;

public class SessionManager {

    private UserSession currentUser;

    public void setCurrentUser(UserSession currentUser) {
        this.currentUser = currentUser;
    }

    public UserSession getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.role());
    }

    public void clear() {
        currentUser = null;
    }
}
