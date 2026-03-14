package com.barterbay.barterbay.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {

        /* LEFT PANEL */

        Label logo = new Label("BarterBay");
        logo.setStyle("-fx-text-fill:white; -fx-font-size:42px; -fx-font-weight:bold;");

        Label tagline = new Label("Trade Smart. Exchange Anything.");
        tagline.setStyle("-fx-text-fill:white; -fx-font-size:16px;");

        VBox leftPanel = new VBox(20, logo, tagline);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(420);

        leftPanel.setStyle("""
                -fx-background-color: linear-gradient(to bottom,#3c73c8,#00bcd4);
                """);

        /* LOGIN FORM */

        Label loginTitle = new Label("Login");
        loginTitle.setStyle("-fx-font-size:28px; -fx-font-weight:bold;");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setPrefWidth(320);
        username.setPrefHeight(40);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setPrefWidth(320);
        password.setPrefHeight(40);

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(320);
        loginBtn.setPrefHeight(40);

        loginBtn.setStyle("""
                -fx-background-color:#3c73c8;
                -fx-text-fill:white;
                -fx-font-size:14px;
                -fx-background-radius:6;
                """);

        Hyperlink registerLink = new Hyperlink("Create an account");
        registerLink.setStyle("-fx-text-fill:#1e88e5;");

        VBox form = new VBox(18, loginTitle, username, password, loginBtn, registerLink);
        form.setAlignment(Pos.CENTER);

        VBox rightPanel = new VBox(form);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(480);
        rightPanel.setPadding(new Insets(40));
        rightPanel.setStyle("-fx-background-color:#f4f6f8;");

        HBox root = new HBox(leftPanel, rightPanel);

        Scene scene = new Scene(root, 900, 520);

        /* LOGIN BUTTON */

        loginBtn.setOnAction(e -> {

            try {

                URL url = new URL("http://localhost:9099/login");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{ \"username\":\"" + username.getText() +
                        "\", \"password\":\"" + password.getText() + "\" }";

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                String response = br.readLine();

                new Alert(Alert.AlertType.INFORMATION, response).show();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        /* OPEN REGISTER PAGE */

        registerLink.setOnAction(e -> {

            RegisterPage page = new RegisterPage();
            page.start(new Stage());

        });

        stage.setTitle("BarterBay Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}