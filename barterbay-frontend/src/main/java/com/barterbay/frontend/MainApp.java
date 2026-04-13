package com.barterbay.frontend;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static com.barterbay.frontend.model.Product selectedProductForExchange;

    public static void setSelectedProductForExchange(com.barterbay.frontend.model.Product product) {
        selectedProductForExchange = product;
    }

    public static com.barterbay.frontend.model.Product getSelectedProductForExchange() {
        return selectedProductForExchange;
    }

    public static void clearSelectedProductForExchange() {
        selectedProductForExchange = null;
    }

    @Override
    public void start(Stage stage) throws IOException {
        setPrimaryStage(stage);
        switchScene("/view/login.fxml", "BarterBay Login", 1120, 720);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
    }

    private static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlPath, String title, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(MainApp.class.getResource("/view/theme.css").toExternalForm());

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void switchScene(String fxmlPath, String title) throws IOException {
        Scene currentScene = primaryStage.getScene();
        double width = currentScene == null ? 1120 : currentScene.getWidth();
        double height = currentScene == null ? 720 : currentScene.getHeight();
        switchScene(fxmlPath, title, width, height);
    }

    public static void main(String[] args) {
        launch();
    }
}