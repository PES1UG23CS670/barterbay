package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barterbay.frontend.model.ExchangeRequest;
import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExchangesController {

    @FXML
    private VBox exchangesContainer;

    @FXML
    private Button backBtn;

    @FXML
    private Button logoutBtn;

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();
    private Map<String, Product> productCache = new HashMap<>();

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> handleBackButton());
        loadExchanges();
    }

    private void handleBackButton() {
        try {
            ServiceRegistry.navigationService().openBrowseProducts();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate: " + e.getMessage());
        }
    }

    private void loadExchanges() {
        try {
            String currentUserId = sessionManager.getCurrentUser().id();
            
            // Load pending exchanges where user is receiver
            final List<ExchangeRequest> pendingExchanges;
            try {
                pendingExchanges = productService.getPendingExchanges(currentUserId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load exchanges: " + e.getMessage());
                return;
            }
            
            // Load negotiating exchanges where user is receiver
            final List<ExchangeRequest> receiverNegotiatingExchanges;
            try {
                receiverNegotiatingExchanges = productService.getNegotiatingExchangesForReceiver(currentUserId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load exchanges: " + e.getMessage());
                return;
            }
            
            // Load negotiating exchanges where user is requester
            final List<ExchangeRequest> requesterNegotiatingExchanges;
            try {
                requesterNegotiatingExchanges = productService.getNegotiatingExchangesForRequester(currentUserId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load exchanges: " + e.getMessage());
                return;
            }
            
            // Load completed exchanges where user is requester
            final List<ExchangeRequest> completedExchanges;
            try {
                completedExchanges = productService.getCompletedRequesterExchanges(currentUserId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load exchanges: " + e.getMessage());
                return;
            }

            exchangesContainer.getChildren().clear();
            productCache.clear();

            // Display pending exchanges (receiver side)
            if (!pendingExchanges.isEmpty()) {
                Label pendingTitle = new Label("Pending Exchange Requests (Receiving):");
                pendingTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0;");
                exchangesContainer.getChildren().add(pendingTitle);
                
                for (ExchangeRequest exchange : pendingExchanges) {
                    VBox card = createExchangeCard(exchange, true);
                    exchangesContainer.getChildren().add(card);
                }
            }

            // Display negotiating exchanges (receiver side - waiting for requester to confirm)
            if (!receiverNegotiatingExchanges.isEmpty()) {
                Label negotiatingTitle = new Label("Negotiating Exchange Requests (Awaiting Requester Response):");
                negotiatingTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
                exchangesContainer.getChildren().add(negotiatingTitle);
                
                for (ExchangeRequest exchange : receiverNegotiatingExchanges) {
                    VBox card = createExchangeCard(exchange, true);
                    exchangesContainer.getChildren().add(card);
                }
            }

            // Display negotiating exchanges (requester side - you sent updated offer)
            if (!requesterNegotiatingExchanges.isEmpty()) {
                java.util.List<ExchangeRequest> inProgressExchanges = new java.util.ArrayList<>();
                java.util.List<ExchangeRequest> maxReachedExchanges = new java.util.ArrayList<>();
                
                for (ExchangeRequest exchange : requesterNegotiatingExchanges) {
                    if (exchange.getNegotiationCount() >= 2) {
                        maxReachedExchanges.add(exchange);
                    } else {
                        inProgressExchanges.add(exchange);
                    }
                }
                
                if (!inProgressExchanges.isEmpty()) {
                    Label requesterNegotiatingTitle = new Label("Negotiation in Progress (Your Requests):");
                    requesterNegotiatingTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
                    exchangesContainer.getChildren().add(requesterNegotiatingTitle);
                    
                    for (ExchangeRequest exchange : inProgressExchanges) {
                        VBox card = createExchangeCard(exchange, false);
                        exchangesContainer.getChildren().add(card);
                    }
                }
                
                if (!maxReachedExchanges.isEmpty()) {
                    Label maxReachedTitle = new Label("Awaiting Response (Max Negotiations Reached):");
                    maxReachedTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0 0 0; -fx-text-fill: #dc3545;");
                    exchangesContainer.getChildren().add(maxReachedTitle);
                    
                    for (ExchangeRequest exchange : maxReachedExchanges) {
                        VBox card = createExchangeCard(exchange, false);
                        exchangesContainer.getChildren().add(card);
                    }
                }
            }

            // Display completed exchanges (requester side)
            if (!completedExchanges.isEmpty()) {
                Label completedTitle = new Label("Exchange History (Your Requests):");
                completedTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
                exchangesContainer.getChildren().add(completedTitle);
                
                for (ExchangeRequest exchange : completedExchanges) {
                    VBox card = createExchangeCard(exchange, false);
                    exchangesContainer.getChildren().add(card);
                }
            }

            if (pendingExchanges.isEmpty() && receiverNegotiatingExchanges.isEmpty() && 
                requesterNegotiatingExchanges.isEmpty() && completedExchanges.isEmpty()) {
                Label noExchanges = new Label("No exchange requests");
                noExchanges.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                exchangesContainer.getChildren().add(noExchanges);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load exchanges: " + e.getMessage());
        }
    }

    private VBox createExchangeCard(ExchangeRequest exchange, boolean isReceiver) {
        VBox card = new VBox(12);
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 16; -fx-border-radius: 4; -fx-background-color: #f9fafb;");
        
        // Debug: Print exchange ID
        if (exchange.getId() == null) {
            System.out.println("DEBUG - Exchange ID is null! Full exchange: " + exchange);
        }
        card.setPrefWidth(Double.MAX_VALUE);

        // Header with status
        HBox header = new HBox(10);
        String userLabel = isReceiver ? "From Requester: " : "To User: ";
        String userId = isReceiver ? exchange.getRequesterId() : exchange.getReceiverId();
        Label userIdLabel = new Label(userLabel + userId.substring(0, 8) + "...");
        userIdLabel.setStyle("-fx-font-weight: bold;");
        
        String statusText = exchange.getStatus();
        String statusColor = getStatusColor(statusText);
        Label statusLabel = new Label("Status: " + statusText);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
        header.getChildren().addAll(userIdLabel, statusLabel);

        // Product details
        VBox details = new VBox(8);
        
        // Requested Product
        Label requestedTitle = new Label("Requested Product:");
        requestedTitle.setStyle("-fx-font-weight: bold;");
        Product requestedProduct = getProductFromCache(exchange.getRequestedProductId());
        String requestedName = requestedProduct != null ? requestedProduct.getItemName() : "Unknown";
        double requestedPrice = requestedProduct != null ? requestedProduct.getPrice() : 0;
        Label requestedLabel = new Label(requestedName + " (₹" + requestedPrice + ")");
        requestedLabel.setStyle("-fx-text-fill: #333;");
        
        // Offered Products
        Label offeringTitle = new Label("Offered Products:");
        offeringTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        
        VBox offeredProducts = new VBox(4);
        List<String> offeredIds = exchange.getOfferedProductIds();
        double totalOfferedPrice = 0;
        if (offeredIds != null && !offeredIds.isEmpty()) {
            for (String productId : offeredIds) {
                Product product = getProductFromCache(productId);
                String productName = product != null ? product.getItemName() : "Unknown";
                double price = product != null ? product.getPrice() : 0;
                totalOfferedPrice += price;
                Label productLabel = new Label("  • " + productName + " (₹" + price + ")");
                offeredProducts.getChildren().add(productLabel);
            }
        } else {
            Label noProducts = new Label("  No products offered");
            offeredProducts.getChildren().add(noProducts);
        }
        Label totalLabel = new Label("  Total Value: ₹" + totalOfferedPrice);
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
        offeredProducts.getChildren().add(totalLabel);
        
        details.getChildren().addAll(requestedTitle, requestedLabel, offeringTitle, offeredProducts);

        card.getChildren().add(header);
        card.getChildren().add(details);

        // Add action buttons only for pending exchanges (receiver side)
        if (isReceiver) {
            HBox buttons = new HBox(10);
            buttons.setPadding(new Insets(12, 0, 0, 0));

            Button acceptBtn = new Button("Accept");
            acceptBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12;");
            acceptBtn.setOnAction(e -> handleAccept(exchange.getId()));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #dc3545; -fx-text-fill: white;");
            rejectBtn.setOnAction(e -> handleReject(exchange.getId()));

            Button negotiateBtn = new Button("Negotiate");
            negotiateBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #ffc107; -fx-text-fill: black;");
            
            // Disable negotiate if max negotiations reached
            if (exchange.getNegotiationCount() >= 2) {
                negotiateBtn.setDisable(true);
                negotiateBtn.setText("Negotiate (Max reached)");
            }
            negotiateBtn.setOnAction(e -> handleNegotiate(exchange.getId()));

            buttons.getChildren().addAll(acceptBtn, rejectBtn, negotiateBtn);
            
            // Add negotiation info
            Label negotiationInfo = new Label("Negotiation attempt: " + exchange.getNegotiationCount() + "/2");
            negotiationInfo.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
            buttons.getChildren().add(negotiationInfo);
            
            card.getChildren().add(buttons);
        } else if (exchange.getStatus().equals("PENDING") && !isReceiver) {
            // Show modify button for requester when status is PENDING
            HBox modifyButtons = new HBox(10);
            modifyButtons.setPadding(new Insets(12, 0, 0, 0));
            
            Button modifyBtn = new Button("Modify Offer");
            modifyBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #17a2b8; -fx-text-fill: white;");
            modifyBtn.setOnAction(e -> handleRenegotiate(exchange));
            
            modifyButtons.getChildren().add(modifyBtn);
            card.getChildren().add(modifyButtons);
        } else if (exchange.getStatus().equals("NEGOTIATING") && exchange.getNegotiationCount() < 2) {
            // Show renegotiate button for requester when status is NEGOTIATING and max not reached
            HBox renegotiateButtons = new HBox(10);
            renegotiateButtons.setPadding(new Insets(12, 0, 0, 0));
            
            Button renegotiateBtn = new Button("Modify & Send");
            renegotiateBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #007bff; -fx-text-fill: white;");
            renegotiateBtn.setOnAction(e -> handleRenegotiate(exchange));
            
            Label negotiationInfo = new Label("Negotiation attempt: " + exchange.getNegotiationCount() + "/2");
            negotiationInfo.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
            
            renegotiateButtons.getChildren().addAll(renegotiateBtn, negotiationInfo);
            card.getChildren().add(renegotiateButtons);
        } else if (exchange.getStatus().equals("NEGOTIATING") && exchange.getNegotiationCount() >= 2) {
            // Show waiting message when max negotiations reached
            HBox waitingButtons = new HBox(10);
            waitingButtons.setPadding(new Insets(12, 0, 0, 0));
            
            Label waitingInfo = new Label("Waiting for receiver to accept or reject... (Max negotiations reached: " + exchange.getNegotiationCount() + "/2)");
            waitingInfo.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
            
            waitingButtons.getChildren().add(waitingInfo);
            card.getChildren().add(waitingButtons);
        }

        return card;
    }

    private String getStatusColor(String status) {
        switch(status) {
            case "ACCEPTED": return "#28a745";
            case "REJECTED": return "#dc3545";
            case "NEGOTIATING": return "#ffc107";
            default: return "#666";
        }
    }

    private Product getProductFromCache(String productId) {
        if (!productCache.containsKey(productId)) {
            try {
                Product product = productService.getProductById(productId);
                productCache.put(productId, product);
            } catch (IOException e) {
                System.err.println("Failed to fetch product: " + productId);
                return null;
            }
        }
        return productCache.get(productId);
    }

    private void handleAccept(String exchangeId) {
        try {
            productService.acceptExchange(exchangeId);
            showAlert("Success", "Exchange accepted!");
            loadExchanges();
        } catch (IOException e) {
            showAlert("Error", "Failed to accept exchange");
            e.printStackTrace();
        }
    }

    private void handleReject(String exchangeId) {
        try {
            productService.rejectExchange(exchangeId);
            showAlert("Success", "Exchange rejected!");
            loadExchanges();
        } catch (IOException e) {
            showAlert("Error", "Failed to reject exchange");
            e.printStackTrace();
        }
    }

    private void handleNegotiate(String exchangeId) {
        try {
            productService.negotiateExchange(exchangeId);
            showAlert("Success", "Negotiation started!");
            loadExchanges();
        } catch (IOException e) {
            showAlert("Error", "Failed to negotiate exchange");
            e.printStackTrace();
        }
    }

    private void handleRenegotiate(ExchangeRequest exchange) {
        try {
            String currentUserId = sessionManager.getCurrentUser().id();
            List<Product> yourProducts = productService.getMyProducts(currentUserId);

            // Show dialog with products to select
            VBox dialogLayout = new VBox(10);
            dialogLayout.setStyle("-fx-padding: 20;");

            Label titleLabel = new Label(exchange.getStatus().equals("PENDING") ? "Modify Your Offer" : "Renegotiate");
            titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            Product requestedProduct = productService.getProductById(exchange.getRequestedProductId());
            Label requestedLabel = new Label("Requested Product: " + 
                (requestedProduct != null ? requestedProduct.getItemName() : "Unknown") + 
                " (₹" + (requestedProduct != null ? requestedProduct.getPrice() : 0) + ")");
            requestedLabel.setStyle("-fx-text-fill: #333; -fx-padding: 10;");

            Label offerLabel = new Label(exchange.getStatus().equals("PENDING") ? 
                "Select Your Products to Offer (Add or Remove):" :
                "Select Your Products to Offer (Add or Modify):");
            offerLabel.setStyle("-fx-font-weight: bold;");

            VBox productsBox = new VBox(8);
            java.util.List<CheckBox> productCheckboxes = new java.util.ArrayList<>();
            
            // Get previously offered product IDs
            java.util.List<String> previouslyOffered = exchange.getOfferedProductIds();
            if (previouslyOffered == null) {
                previouslyOffered = new java.util.ArrayList<>();
            }
            final java.util.List<String> finalPreviouslyOffered = previouslyOffered;
            
            // Separate products into previously offered and available to add
            java.util.List<Product> previousProducts = new java.util.ArrayList<>();
            java.util.List<Product> availableProducts = new java.util.ArrayList<>();
            
            for (Product product : yourProducts) {
                if (finalPreviouslyOffered.contains(product.getId())) {
                    previousProducts.add(product);
                } else {
                    availableProducts.add(product);
                }
            }
            
            // Display previously offered products
            if (!previousProducts.isEmpty()) {
                Label previousTitle = new Label("Previously Offered Products (Currently Selected):");
                previousTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-text-fill: #0275d8;");
                productsBox.getChildren().add(previousTitle);
                
                for (Product product : previousProducts) {
                    CheckBox cb = new CheckBox(product.getItemName() + " (₹" + product.getPrice() + ")");
                    cb.setUserData(product);
                    cb.setSelected(true);
                    cb.setStyle("-fx-font-weight: bold;");
                    productsBox.getChildren().add(cb);
                    productCheckboxes.add(cb);
                }
            }
            
            // Display available products to add
            if (!availableProducts.isEmpty()) {
                Label availableTitle = new Label("Available Products to Add:");
                availableTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-text-fill: #28a745;");
                productsBox.getChildren().add(availableTitle);
                
                for (Product product : availableProducts) {
                    CheckBox cb = new CheckBox(product.getItemName() + " (₹" + product.getPrice() + ")");
                    cb.setUserData(product);
                    cb.setSelected(false);
                    productsBox.getChildren().add(cb);
                    productCheckboxes.add(cb);
                }
            }

            Button submitBtn = new Button(exchange.getStatus().equals("PENDING") ? "Update Offer" : "Resubmit Offer");
            submitBtn.setStyle("-fx-padding: 8 20;");

            Button addNewProductBtn = new Button("+ Add New Product");
            addNewProductBtn.setStyle("-fx-padding: 8 20; -fx-background-color: #28a745; -fx-text-fill: white;");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-padding: 8 20;");

            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(submitBtn, addNewProductBtn, cancelBtn);

            dialogLayout.getChildren().addAll(titleLabel, requestedLabel, offerLabel, productsBox, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.control.ScrollPane(dialogLayout), 500, 600);
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle(exchange.getStatus().equals("PENDING") ? "Modify Offer" : "Renegotiate Exchange");
            dialog.setScene(scene);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            addNewProductBtn.setOnAction(e -> {
                // Collect currently selected products before refreshing
                java.util.List<String> currentlySelectedIds = new java.util.ArrayList<>();
                for (CheckBox cb : productCheckboxes) {
                    if (cb.isSelected()) {
                        Product p = (Product) cb.getUserData();
                        currentlySelectedIds.add(p.getId());
                    }
                }
                
                handleAddNewProduct((refreshedProductList) -> {
                    // Refresh products list in the dialog
                    productsBox.getChildren().clear();
                    productCheckboxes.clear();
                    
                    java.util.List<Product> updatedPreviousProducts = new java.util.ArrayList<>();
                    java.util.List<Product> updatedAvailableProducts = new java.util.ArrayList<>();
                    
                    for (Product product : refreshedProductList) {
                        if (finalPreviouslyOffered.contains(product.getId()) || currentlySelectedIds.contains(product.getId())) {
                            updatedPreviousProducts.add(product);
                        } else {
                            updatedAvailableProducts.add(product);
                        }
                    }
                    
                    if (!updatedPreviousProducts.isEmpty()) {
                        Label previousTitle = new Label("Previously Offered Products (Currently Selected):");
                        previousTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-text-fill: #0275d8;");
                        productsBox.getChildren().add(previousTitle);
                        
                        for (Product product : updatedPreviousProducts) {
                            CheckBox cb = new CheckBox(product.getItemName() + " (₹" + product.getPrice() + ")");
                            cb.setUserData(product);
                            cb.setSelected(true);
                            cb.setStyle("-fx-font-weight: bold;");
                            productsBox.getChildren().add(cb);
                            productCheckboxes.add(cb);
                        }
                    }
                    
                    if (!updatedAvailableProducts.isEmpty()) {
                        Label availableTitle = new Label("Available Products to Add:");
                        availableTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-text-fill: #28a745;");
                        productsBox.getChildren().add(availableTitle);
                        
                        for (Product product : updatedAvailableProducts) {
                            CheckBox cb = new CheckBox(product.getItemName() + " (₹" + product.getPrice() + ")");
                            cb.setUserData(product);
                            cb.setSelected(false);
                            productsBox.getChildren().add(cb);
                            productCheckboxes.add(cb);
                        }
                    }
                });
            });

            submitBtn.setOnAction(e -> {
                java.util.List<String> selectedProductIds = new java.util.ArrayList<>();
                double totalOfferedValue = 0;
                
                for (CheckBox cb : productCheckboxes) {
                    if (cb.isSelected()) {
                        Product p = (Product) cb.getUserData();
                        selectedProductIds.add(p.getId());
                        totalOfferedValue += p.getPrice();
                    }
                }

                if (selectedProductIds.isEmpty()) {
                    showAlert("Error", "Please select at least one product");
                    return;
                }

                // Validate that offered products value >= requested product value
                double requestedProductValue = requestedProduct != null ? requestedProduct.getPrice() : 0;
                if (totalOfferedValue < requestedProductValue) {
                    showAlert("Error", "The total value of your offered products (₹" + totalOfferedValue + 
                        ") must be at least equal to the requested product value (₹" + requestedProductValue + ")");
                    return;
                }

                try {
                    // If status is PENDING, just update the offer without incrementing negotiation count
                    // If status is NEGOTIATING, renegotiate (increments count and goes back to PENDING)
                    if (exchange.getStatus().equals("PENDING")) {
                        productService.updateOfferProducts(exchange.getId(), selectedProductIds);
                        showAlert("Success", "Offer updated!");
                    } else if (exchange.getStatus().equals("NEGOTIATING")) {
                        productService.renegotiateExchange(exchange.getId(), selectedProductIds);
                        showAlert("Success", "Offer resubmitted for negotiation!");
                    }
                    dialog.close();
                    loadExchanges();
                } catch (IOException ex) {
                    showAlert("Error", "Failed to update offer: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());

            dialog.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
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

    @FunctionalInterface
    interface ProductListCallback {
        void onProductsRefreshed(java.util.List<Product> products);
    }

    private void handleAddNewProduct(ProductListCallback callback) {
        String currentUserId = sessionManager.getCurrentUser().id();
        
        // Create dialog for adding new product
        VBox dialogLayout = new VBox(10);
        dialogLayout.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("Add New Product");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label itemNameLabel = new Label("Product Name:");
        javafx.scene.control.TextField itemNameField = new javafx.scene.control.TextField();
        itemNameField.setPromptText("Enter product name");

        Label categoryLabel = new Label("Category:");
        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        categoryCombo.getItems().addAll("electronics", "toys", "books", "furniture", "clothing", "sports", "not applicable");
        categoryCombo.setStyle("-fx-padding: 5;");

        Label descriptionLabel = new Label("Description:");
        javafx.scene.control.TextArea descriptionArea = new javafx.scene.control.TextArea();
        descriptionArea.setPromptText("Enter product description");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        Label priceLabel = new Label("Price (₹):");
        javafx.scene.control.TextField priceField = new javafx.scene.control.TextField();
        priceField.setPromptText("Enter price");

        Button saveBtn = new Button("Add Product");
        saveBtn.setStyle("-fx-padding: 8 20; -fx-background-color: #28a745; -fx-text-fill: white;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-padding: 8 20;");

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);

        dialogLayout.getChildren().addAll(
            titleLabel,
            itemNameLabel, itemNameField,
            categoryLabel, categoryCombo,
            descriptionLabel, descriptionArea,
            priceLabel, priceField,
            buttonBox
        );

        javafx.scene.Scene scene = new javafx.scene.Scene(dialogLayout, 400, 500);
        javafx.stage.Stage newProductDialog = new javafx.stage.Stage();
        newProductDialog.setTitle("Add New Product");
        newProductDialog.setScene(scene);
        newProductDialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        saveBtn.setOnAction(e -> {
            String itemName = itemNameField.getText().trim();
            String category = categoryCombo.getValue();
            String description = descriptionArea.getText().trim();
            String priceStr = priceField.getText().trim();

            if (itemName.isEmpty()) {
                showAlert("Error", "Please enter product name");
                return;
            }
            if (category == null) {
                showAlert("Error", "Please select a category");
                return;
            }
            if (description.isEmpty()) {
                showAlert("Error", "Please enter description");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid price");
                return;
            }

            try {
                Product newProduct = new Product(itemName, category, description, price, currentUserId);
                productService.addProduct(newProduct);
                showAlert("Success", "Product added successfully!");
                newProductDialog.close();
                
                // Refresh product list and call callback
                java.util.List<Product> refreshedProducts = productService.getMyProducts(currentUserId);
                callback.onProductsRefreshed(refreshedProducts);
            } catch (IOException ex) {
                showAlert("Error", "Failed to add product: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.setOnAction(e -> newProductDialog.close());

        newProductDialog.showAndWait();
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
