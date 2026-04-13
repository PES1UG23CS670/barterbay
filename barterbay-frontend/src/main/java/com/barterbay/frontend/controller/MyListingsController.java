package com.barterbay.frontend.controller;

import java.io.IOException;

import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class MyListingsController {

    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;

    @FXML private FlowPane productContainer;
    @FXML private Button logoutBtn;

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();

    @FXML
    public void initialize() {
        loadProducts();
    }

    @FXML
    public void handleAddProduct() {
        try {
            Product product = new Product(
                    titleField.getText(),
                    categoryField.getText(),
                    descField.getText(),
                    Double.parseDouble(priceField.getText()),
                    sessionManager.getCurrentUser().id()
            );

            productService.addProduct(product);
            clearForm();
            loadProducts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        try {
            productContainer.getChildren().clear();

            var products = productService.getMyProducts(
                    sessionManager.getCurrentUser().id()
            );

            for (Product p : products) {
                productContainer.getChildren().add(createCard(p));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCard(Product p) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label name = new Label(p.getItemName());
        name.getStyleClass().add("title");

        Label price = new Label("₹" + p.getPrice());

        Label desc = new Label(p.getDescription());
        desc.getStyleClass().add("muted");

        card.getChildren().addAll(name, price, desc);
        return card;
    }

    private void clearForm() {
        titleField.clear();
        descField.clear();
        categoryField.clear();
        priceField.clear();
    }

    @FXML
    public void goToBrowse() throws IOException {
        ServiceRegistry.navigationService().openBrowseProducts();
    }

    @FXML
    public void goToExchanges() {
        System.out.println("Exchanges clicked");
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