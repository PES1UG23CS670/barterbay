package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExchangeController {

    @FXML
    private Label requestedProductLabel;

    @FXML
    private Label requestedProductDetails;

    @FXML
    private Label yourProductsLabel;

    @FXML
    private FlowPane yourProductsContainer;

    @FXML
    private Button addNewProductBtn;

    @FXML
    private Button acceptBtn;

    @FXML
    private Button backBtn;

    private Product selectedProductForExchange;
    private List<Product> selectedYourProducts = new ArrayList<>();

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();

    @FXML
    public void initialize() {
        selectedProductForExchange = MainApp.getSelectedProductForExchange();

        if (selectedProductForExchange != null) {
            requestedProductLabel.setText("Product You Want:");
            requestedProductDetails.setText(
                "Name: " + selectedProductForExchange.getItemName() + "\n" +
                "Category: " + selectedProductForExchange.getCategory() + "\n" +
                "Price: ₹" + selectedProductForExchange.getPrice() + "\n" +
                "Description: " + selectedProductForExchange.getDescription()
            );
        }

        loadYourProducts();
    }

    private void loadYourProducts() {
        try {
            String currentUserId = sessionManager.getCurrentUser().id();
            List<Product> yourProducts = productService.getMyProducts(currentUserId);

            yourProductsContainer.getChildren().clear();
            // Preserve previously selected products
            List<String> previouslySelectedIds = new ArrayList<>();
            for (Product p : selectedYourProducts) {
                previouslySelectedIds.add(p.getId());
            }
            selectedYourProducts.clear();

            for (Product product : yourProducts) {
                VBox card = createProductCard(product);
                yourProductsContainer.getChildren().add(card);
                
                // Restore selection for previously selected products
                if (previouslySelectedIds.contains(product.getId())) {
                    selectedYourProducts.add(product);
                    // Mark checkbox as selected visually
                    for (javafx.scene.Node node : card.getChildren()) {
                        if (node instanceof CheckBox) {
                            ((CheckBox) node).setSelected(true);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load your products");
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 12; -fx-border-radius: 4; -fx-min-width: 180;");
        card.setPrefWidth(180);
        card.setPrefHeight(200);

        CheckBox selectBtn = new CheckBox();
        selectBtn.setOnAction(e -> {
            if (selectBtn.isSelected()) {
                if (!selectedYourProducts.contains(product)) {
                    selectedYourProducts.add(product);
                }
            } else {
                selectedYourProducts.remove(product);
            }
            System.out.println("DEBUG - Selected products: " + selectedYourProducts.size());
        });

        Label name = new Label(product.getItemName());
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label price = new Label("₹" + product.getPrice());
        Label category = new Label(product.getCategory());
        category.setStyle("-fx-text-fill: #666;");

        Label desc = new Label(product.getDescription());
        desc.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
        desc.setWrapText(true);

        card.getChildren().addAll(selectBtn, name, price, category, desc);
        return card;
    }

    @FXML
    public void handleAddNewProduct() {
        showAddProductDialog();
    }

    private void showAddProductDialog() {
        VBox dialogLayout = new VBox(10);
        dialogLayout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label titleLabel = new Label("Add New Product");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefHeight(80);
        descField.setWrapText(true);

        Button submitBtn = new Button("Add Product");
        Button cancelBtn = new Button("Cancel");

        submitBtn.setStyle("-fx-padding: 8 20;");
        cancelBtn.setStyle("-fx-padding: 8 20;");

        VBox buttonsBox = new VBox(10);
        buttonsBox.getChildren().addAll(submitBtn, cancelBtn);

        dialogLayout.getChildren().addAll(
            titleLabel, nameField, categoryField, priceField, descField, buttonsBox
        );

        Scene scene = new Scene(dialogLayout, 400, 450);
        Stage dialog = new Stage();
        dialog.setTitle("Add New Product");
        dialog.setScene(scene);
        dialog.initModality(Modality.APPLICATION_MODAL);

        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String price = priceField.getText().trim();
            String desc = descField.getText().trim();

            if (name.isEmpty() || category.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                showAlert("Error", "Please fill all fields");
                return;
            }

            try {
                double priceValue = Double.parseDouble(price);
                String userId = sessionManager.getCurrentUser().id();
                Product newProduct = new Product(name, category, desc, priceValue, userId);

                Product savedProduct = productService.addProduct(newProduct);
                
                // Auto-select the newly added product
                selectedYourProducts.add(savedProduct);
                System.out.println("DEBUG - New product auto-selected: " + savedProduct.getId());
                
                dialog.close();
                loadYourProducts();

                showAlert("Success", "Product added and selected for exchange!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Price must be a number");
            } catch (IOException ex) {
                showAlert("Error", "Failed to add product");
                ex.printStackTrace();
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        dialog.showAndWait();
    }

    @FXML
    public void handleAccept() {
        if (selectedProductForExchange == null) {
            showAlert("Error", "No product selected to exchange");
            return;
        }

        if (selectedYourProducts.isEmpty()) {
            showAlert("Error", "Please select at least one product to offer");
            return;
        }

        // Validate that total offered product price >= requested product price
        double requestedProductPrice = selectedProductForExchange.getPrice();
        double totalOfferedPrice = 0;
        for (Product p : selectedYourProducts) {
            totalOfferedPrice += p.getPrice();
        }

        if (totalOfferedPrice < requestedProductPrice) {
            showAlert("Error", 
                "Total value of offered products (₹" + totalOfferedPrice + ") must be greater than or equal to requested product value (₹" + requestedProductPrice + ")");
            return;
        }

        try {
            String currentUserId = sessionManager.getCurrentUser().id();
            
            System.out.println("DEBUG - Submitting exchange request:");
            System.out.println("  Requester ID: " + currentUserId);
            System.out.println("  Receiver ID: " + selectedProductForExchange.getUserId());
            System.out.println("  Requested Product ID: " + selectedProductForExchange.getId());
            System.out.println("  Requested Product Price: ₹" + requestedProductPrice);
            System.out.println("  Offered Product IDs: " + selectedYourProducts.size());
            System.out.println("  Total Offered Price: ₹" + totalOfferedPrice);
            
            List<String> offeredProductIds = new ArrayList<>();
            for (Product p : selectedYourProducts) {
                offeredProductIds.add(p.getId());
                System.out.println("    - " + p.getId() + " (" + p.getItemName() + ") - ₹" + p.getPrice());
            }
            
            productService.submitExchangeRequest(
                currentUserId,
                selectedProductForExchange.getUserId(),
                selectedProductForExchange.getId(),
                offeredProductIds
            );

            showAlert("Success", "Exchange request submitted!");
            MainApp.clearSelectedProductForExchange();
            ServiceRegistry.navigationService().openBrowseProducts();

        } catch (IOException e) {
            showAlert("Error", "Failed to submit exchange request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack() {
        MainApp.clearSelectedProductForExchange();
        try {
            ServiceRegistry.navigationService().openBrowseProducts();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
