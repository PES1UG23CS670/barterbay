package com.barterbay.frontend.service;

import java.io.IOException;

import com.barterbay.frontend.MainApp;

public class NavigationService {

    public void openLogin() throws IOException {
        MainApp.switchScene("/view/login.fxml", "BarterBay Login");
    }

    public void openSignup() throws IOException {
        MainApp.switchScene("/view/signup.fxml", "BarterBay Signup");
    }

    public void openAdminDashboard() throws IOException {
        MainApp.switchScene("/view/admin-dashboard.fxml", "BarterBay Admin Dashboard");
    }

    public void openUserDashboard() throws IOException {
        MainApp.switchScene("/view/user-dashboard.fxml", "BarterBay Dashboard");
    }
}
