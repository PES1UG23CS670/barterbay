package com.barterbay.barterbay.fx;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterPage {

    public void start(Stage stage){

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button registerBtn = new Button("Register");

        VBox root = new VBox(15, title, username, password, registerBtn);
        root.setAlignment(Pos.CENTER);

        registerBtn.setOnAction(e -> {

            try {

                URL url = new URL("http://localhost:9099/register");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{ \"username\":\""+username.getText()+"\", \"password\":\""+password.getText()+"\" }";

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                String response = br.readLine();

                Alert alert;

                if(response.contains("exists")){
                    alert = new Alert(Alert.AlertType.ERROR, response);
                } else {
                    alert = new Alert(Alert.AlertType.INFORMATION, response);
                }

                alert.show();

            }
            catch(Exception ex){
                ex.printStackTrace();
            }

        });

        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }
}