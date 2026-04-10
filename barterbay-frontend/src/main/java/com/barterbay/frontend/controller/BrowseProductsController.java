package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

public class BrowseProductsController {

    @FXML
    private FlowPane productContainer;

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

            String currentUserId = sessionManager.getCurrentUser().id();

            for (Product product : products) {

                // skip invalid data
                if (product.getUserId() == null) continue;
                if (product.getItemName() == null) continue;

                // skip my own products
                if (product.getUserId().equals(currentUserId)) continue;

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

        exchangeBtn.setOnAction(e -> {
            System.out.println("exchange button clicked!");
        });

        layout.getChildren().addAll(name, category, price, desc, exchangeBtn);

        Scene scene = new Scene(layout);

        Stage popup = new Stage();
        popup.setTitle("Product Details");
        popup.setScene(scene);

        // makes it modal (blocks background)
        popup.initModality(Modality.APPLICATION_MODAL);

        popup.showAndWait();
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
    public void goToExchanges() {
        System.out.println("Exchanges clicked");
    }
}