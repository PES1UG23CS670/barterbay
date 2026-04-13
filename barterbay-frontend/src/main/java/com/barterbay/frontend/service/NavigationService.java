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

    public void openBrowseProducts() throws IOException {
        MainApp.switchScene("/view/post-login.fxml", "BarterBay Browse Products");
    }

    public void openMyListings() throws IOException {
        MainApp.switchScene("/view/my-listings.fxml", "My Listings");
    }

    public void openExchange() throws IOException {
        MainApp.switchScene("/view/exchange.fxml", "Exchange Request");
    }

    public void openExchanges() throws IOException {
        MainApp.switchScene("/view/exchanges.fxml", "Pending Exchange Requests");
    }
} 
