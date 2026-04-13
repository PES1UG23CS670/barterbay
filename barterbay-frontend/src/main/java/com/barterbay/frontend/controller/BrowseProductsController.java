package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.List;

import com.barterbay.frontend.MainApp;
import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BrowseProductsController {

    @FXML
    private FlowPane productContainer;
    
    @FXML
    private Button logoutBtn;

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();

    @FXML
    public void initialize() {
        System.out.println("Browse page loaded");
        loadProducts();
    }

    private void loadProducts() {
        try {
            productContainer.getChildren().clear();

            List<Product> products = productService.getAllProducts();

            for (Product product : products) {

                // skip invalid data
                if (product.getUserId() == null) continue;
                if (product.getItemName() == null) continue;

                productContainer.getChildren().add(createCard(product));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCard(Product p) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card"); // same styling as My Listings

        Label name = new Label(p.getItemName());
        name.getStyleClass().add("title");

        Label price = new Label("₹" + p.getPrice());

        Label desc = new Label(p.getDescription());
        desc.getStyleClass().add("muted");

        card.getChildren().addAll(name, price, desc);

        card.setOnMouseClicked(e -> {
            showProductPopup(p);
        });

        return card;
    }

    private void showProductPopup(Product p) {

        VBox layout = new VBox(12);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        layout.setPrefWidth(300);

        Label name = new Label(p.getItemName());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label category = new Label("Category: " + p.getCategory());
        Label price = new Label("Price: ₹" + p.getPrice());
        Label desc = new Label(p.getDescription());
        desc.setWrapText(true);

        Button exchangeBtn = new Button("Exchange");

        Scene scene = new Scene(layout);
        Stage popup = new Stage();

        exchangeBtn.setOnAction(e -> {
            // Check if the product belongs to the current user
            String currentUserId = sessionManager.getCurrentUser().id();
            if (currentUserId.equals(p.getUserId())) {
                showAlert("Error", "You cannot select your own product for exchange");
                return;
            }
            
            MainApp.setSelectedProductForExchange(p);
            popup.close();
            try {
                ServiceRegistry.navigationService().openExchange();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(name, category, price, desc, exchangeBtn);

        popup.setTitle("Product Details");
        popup.setScene(scene);

        // makes it modal (blocks background)
        popup.initModality(Modality.APPLICATION_MODAL);

        popup.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToBrowse() {
        System.out.println("Browse clicked");
    }

    @FXML
    public void goToListings() throws IOException {
        ServiceRegistry.navigationService().openMyListings();
    }

    @FXML
    public void goToExchanges() throws IOException {
        ServiceRegistry.navigationService().openExchanges();
    }

    @FXML
    public void logout() {
        sessionManager.clear();
        try {
            ServiceRegistry.navigationService().openLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}