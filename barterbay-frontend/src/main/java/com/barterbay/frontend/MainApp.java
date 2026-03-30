package com.barterbay.frontend;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        // Title
        Label title = new Label("Sign Up");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #1E3A8A; -fx-font-weight: bold;");

        // Username Field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");
        usernameField.setStyle("-fx-pref-width: 250px; -fx-padding: 10px;");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setStyle("-fx-pref-width: 250px; -fx-padding: 10px;");

        // Status Label
        Label status = new Label();
        status.setStyle("-fx-font-size: 14px;");

        // Signup Button
        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 10 25 10 25;" +
                "-fx-background-radius: 8;"
        );

        // 🔥 BUTTON ACTION
        signupBtn.setOnAction(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                // Basic validation
                if (username.isEmpty() || password.isEmpty()) {
                    status.setStyle("-fx-text-fill: red;");
                    status.setText("Please fill all fields!");
                    return;
                }

                URL url = URI.create("http://localhost:8080/api/users/signup?username="
                    + username + "&password=" + password).toURL();

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200 || responseCode == 201) {
                    status.setStyle("-fx-text-fill: green;");
                    status.setText("Signup successful!");

                    // Clear fields
                    usernameField.clear();
                    passwordField.clear();

                } else if (responseCode == 400) {
                    status.setStyle("-fx-text-fill: red;");
                    status.setText("User already exists!");

                } else {
                    status.setStyle("-fx-text-fill: red;");
                    status.setText("Signup failed! Code: " + responseCode);
                }

            } catch (Exception ex) {
                status.setStyle("-fx-text-fill: red;");
                status.setText("Connection error!");
                ex.printStackTrace();
            }
        });

        // Layout
        VBox root = new VBox(15, title, usernameField, passwordField, signupBtn, status);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 40;");

        Scene scene = new Scene(root, 400, 320);

        stage.setTitle("BarterBay Signup");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}