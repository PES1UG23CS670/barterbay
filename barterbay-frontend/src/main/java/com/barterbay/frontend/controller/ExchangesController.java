package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barterbay.frontend.model.ExchangeRequest;
import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ExchangeGateway;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   This controller is now ONLY responsible for:
 *     1. Loading exchange data via ExchangeGateway.
 *     2. Grouping exchanges under section labels.
 *     3. Delegating card building to ExchangeCardFactory.
 *     4. Opening the renegotiate dialog.
 *   Card-building code (~150 lines) extracted → ExchangeCardFactory.
 *   Exchange HTTP calls extracted → ExchangeGateway / ExchangeGatewayImpl.
 *
 * SOLID – Dependency-Inversion Principle (DIP)
 *   Depends on ExchangeGateway (interface), not on ExchangeGatewayImpl.
 *
 * GRASP – Low Coupling
 *   No direct ProductService exchange methods are called here.
 *   Exchange API calls go through ExchangeGateway.
 *   Product lookup only (getMyProducts, getProductById) still uses ProductService.
 *
 * NOTE: Existing FXML bindings (fx:id, onAction) are UNCHANGED.
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/controller/ExchangesController.java
 *   (replaces the existing file)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ExchangesController {

    @FXML private VBox   exchangesContainer;
    @FXML private Button backBtn;
    @FXML private Button logoutBtn;

    // DIP: depend on interfaces / stable types
    private final ExchangeGateway exchangeGateway = ServiceRegistry.exchangeGateway();
    private final ProductService  productService  = ServiceRegistry.productService();
    private final SessionManager  sessionManager  = ServiceRegistry.sessionManager();

    // Shared product cache: populated during loadExchanges, passed to factory
    private final Map<String, Product> productCache = new HashMap<>();

    // Factory is created after productCache is initialised (GRASP Creator)
    private ExchangeCardFactory cardFactory;

    @FXML
    public void initialize() {
        // GRASP Creator: factory gets the cache and the reload callback
        cardFactory = new ExchangeCardFactory(exchangeGateway, productCache, this::loadExchanges);
        backBtn.setOnAction(e -> handleBackButton());
        loadExchanges();
    }

    // =========================================================================
    // LOAD & DISPLAY
    // =========================================================================

    private void loadExchanges() {
        try {
            String userId = sessionManager.getCurrentUser().id();

            List<ExchangeRequest> pending              = exchangeGateway.getPendingForReceiver(userId);
            List<ExchangeRequest> receiverNegotiating  = exchangeGateway.getNegotiatingForReceiver(userId);
            List<ExchangeRequest> requesterNegotiating = exchangeGateway.getNegotiatingForRequester(userId);
            List<ExchangeRequest> completed            = exchangeGateway.getCompletedForRequester(userId);

            exchangesContainer.getChildren().clear();
            productCache.clear();

            // Pre-fill product cache for all referenced product IDs
            preloadProducts(pending, receiverNegotiating, requesterNegotiating, completed);

            // ── Sections (layout logic unchanged from original) ────────────────
            addSection("Pending Exchange Requests (Receiving):",
                       pending, true);

            addSection("Negotiating Exchange Requests (Awaiting Requester Response):",
                       receiverNegotiating, true);

            // Requester-side negotiating: split by max-reached
            List<ExchangeRequest> inProgress  = requesterNegotiating.stream()
                .filter(e -> e.getNegotiationCount() < 2)
                .collect(java.util.stream.Collectors.toList());
            List<ExchangeRequest> maxReached   = requesterNegotiating.stream()
                .filter(e -> e.getNegotiationCount() >= 2)
                .collect(java.util.stream.Collectors.toList());

            addSection("Negotiation in Progress (Your Requests):", inProgress, false);

            if (!maxReached.isEmpty()) {
                Label title = sectionLabel("Awaiting Response (Max Negotiations Reached):");
                title.setStyle(title.getStyle() + " -fx-text-fill: #dc3545;");
                exchangesContainer.getChildren().add(title);
                maxReached.forEach(ex -> exchangesContainer.getChildren().add(
                    buildCardAndWireRenegotiate(ex, false)));
            }

            addSection("Exchange History (Your Requests):", completed, false);

            if (pending.isEmpty() && receiverNegotiating.isEmpty()
                    && requesterNegotiating.isEmpty() && completed.isEmpty()) {
                Label empty = new Label("No exchange requests");
                empty.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                exchangesContainer.getChildren().add(empty);
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to load exchanges: " + e.getMessage());
        }
    }

    /**
     * Populates productCache for every product ID referenced by any exchange.
     * Centralised here so ExchangeCardFactory.cachedProduct() always hits.
     */
    @SafeVarargs
    private final void preloadProducts(List<ExchangeRequest>... groups) {
        for (List<ExchangeRequest> group : groups) {
            for (ExchangeRequest ex : group) {
                cacheProduct(ex.getRequestedProductId());
                if (ex.getOfferedProductIds() != null) {
                    ex.getOfferedProductIds().forEach(this::cacheProduct);
                }
            }
        }
    }

    private void cacheProduct(String id) {
        if (id == null || productCache.containsKey(id)) return;
        try {
            productCache.put(id, productService.getProductById(id));
        } catch (IOException e) {
            System.err.println("Failed to fetch product: " + id);
        }
    }

    private void addSection(String title, List<ExchangeRequest> exchanges, boolean isReceiver) {
        if (exchanges.isEmpty()) return;
        exchangesContainer.getChildren().add(sectionLabel(title));
        exchanges.forEach(ex -> exchangesContainer.getChildren().add(
            buildCardAndWireRenegotiate(ex, isReceiver)));
    }

    /**
     * Builds the card through the factory.
     * For modify/renegotiate buttons the card factory calls onRefresh → loadExchanges.
     * But the Modify Offer / Modify & Send buttons need to open a dialog first,
     * so we post-process the card here and replace the button action.
     *
     * GRASP – Controller: this is the appropriate place to wire UI events that
     * require session/navigation context not available inside the factory.
     */
    private VBox buildCardAndWireRenegotiate(ExchangeRequest exchange, boolean isReceiver) {
        VBox card = cardFactory.buildCard(exchange, isReceiver);

        // Rewire Modify/Renegotiate buttons so they open the dialog
        if (!isReceiver && ("PENDING".equals(exchange.getStatus())
                || "NEGOTIATING".equals(exchange.getStatus()))) {
            for (javafx.scene.Node node : card.getChildren()) {
                if (node instanceof HBox) {
                    ((HBox) node).getChildren().stream()
                        .filter(n -> n instanceof Button)
                        .map(n -> (Button) n)
                        .filter(b -> b.getText().startsWith("Modify") || b.getText().startsWith("Resubmit"))
                        .forEach(b -> b.setOnAction(e -> handleRenegotiate(exchange)));
                }
            }
        }
        return card;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        return label;
    }

    // =========================================================================
    // RENEGOTIATE DIALOG  (logic unchanged from original)
    // =========================================================================

    private void handleRenegotiate(ExchangeRequest exchange) {
        try {
            String userId = sessionManager.getCurrentUser().id();
            List<Product> yourProducts = productService.getMyProducts(userId);

            VBox dialogLayout = new VBox(10);
            dialogLayout.setStyle("-fx-padding: 20;");

            Label titleLabel = new Label(exchange.getStatus().equals("PENDING") ? "Modify Your Offer" : "Renegotiate");
            titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            Product requestedProduct = productService.getProductById(exchange.getRequestedProductId());
            Label requestedLabel = new Label("Requested Product: " +
                (requestedProduct != null ? requestedProduct.getItemName() : "Unknown") +
                " (₹" + (requestedProduct != null ? requestedProduct.getPrice() : 0) + ")");
            requestedLabel.setStyle("-fx-text-fill: #333; -fx-padding: 10;");

            Label offerLabel = new Label("Select Your Products to Offer:");
            offerLabel.setStyle("-fx-font-weight: bold;");

            VBox productsBox = new VBox(8);
            java.util.List<CheckBox> checkboxes = new java.util.ArrayList<>();

            List<String> previouslyOffered = exchange.getOfferedProductIds() != null
                ? exchange.getOfferedProductIds() : new java.util.ArrayList<>();

            populateProductCheckboxes(productsBox, checkboxes, yourProducts, previouslyOffered);

            Button submitBtn = new Button(exchange.getStatus().equals("PENDING") ? "Update Offer" : "Resubmit Offer");
            submitBtn.setStyle("-fx-padding: 8 20;");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-padding: 8 20;");

            Button addNewBtn = new Button("+ Add New Product");
            addNewBtn.setStyle("-fx-padding: 8 20; -fx-background-color: #28a745; -fx-text-fill: white;");

            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(submitBtn, addNewBtn, cancelBtn);
            dialogLayout.getChildren().addAll(titleLabel, requestedLabel, offerLabel, productsBox, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(
                new javafx.scene.control.ScrollPane(dialogLayout), 500, 600);
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle(exchange.getStatus().equals("PENDING") ? "Modify Offer" : "Renegotiate Exchange");
            dialog.setScene(scene);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            final Product finalRequested = requestedProduct;
            final List<String> finalPrevious = previouslyOffered;

            addNewBtn.setOnAction(e -> {
                List<String> selected = selectedIds(checkboxes);
                handleAddNewProduct(refreshed -> {
                    productsBox.getChildren().clear();
                    checkboxes.clear();
                    populateProductCheckboxes(productsBox, checkboxes, refreshed,
                        mergeIds(finalPrevious, selected));
                });
            });

            submitBtn.setOnAction(e -> {
                List<String> selectedIds = selectedIds(checkboxes);
                if (selectedIds.isEmpty()) { showAlert("Error", "Select at least one product"); return; }

                double total = sumPrices(checkboxes);
                double required = finalRequested != null ? finalRequested.getPrice() : 0;
                if (total < required) {
                    showAlert("Error", "Total offered (₹" + total + ") must be ≥ required (₹" + required + ")");
                    return;
                }
                try {
                    if ("PENDING".equals(exchange.getStatus())) {
                        exchangeGateway.updateOffer(exchange.getId(), selectedIds);
                        showAlert("Success", "Offer updated!");
                    } else {
                        exchangeGateway.renegotiate(exchange.getId(), selectedIds);
                        showAlert("Success", "Offer resubmitted!");
                    }
                    dialog.close();
                    loadExchanges();
                } catch (IOException ex) {
                    showAlert("Error", "Failed: " + ex.getMessage());
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());
            dialog.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    // =========================================================================
    // ADD-NEW-PRODUCT DIALOG  (logic unchanged from original)
    // =========================================================================

    @FunctionalInterface interface ProductListCallback {
        void onProductsRefreshed(List<Product> products);
    }

    private void handleAddNewProduct(ProductListCallback callback) {
        String userId = sessionManager.getCurrentUser().id();
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");

        Label titleLbl = new Label("Add New Product");
        titleLbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Product Name");

        javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
        categoryCombo.getItems().addAll("electronics","toys","books","furniture","clothing","sports","not applicable");

        javafx.scene.control.TextArea descArea = new javafx.scene.control.TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);

        javafx.scene.control.TextField priceField = new javafx.scene.control.TextField();
        priceField.setPromptText("Price (₹)");

        Button save   = new Button("Add Product");
        save.setStyle("-fx-padding: 8 20; -fx-background-color: #28a745; -fx-text-fill: white;");
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-padding: 8 20;");

        HBox btns = new HBox(10);
        btns.getChildren().addAll(save, cancel);
        layout.getChildren().addAll(titleLbl,
            new Label("Name:"), nameField,
            new Label("Category:"), categoryCombo,
            new Label("Description:"), descArea,
            new Label("Price (₹):"), priceField, btns);

        javafx.stage.Stage d = new javafx.stage.Stage();
        d.setTitle("Add New Product");
        d.setScene(new javafx.scene.Scene(layout, 400, 500));
        d.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        save.setOnAction(e -> {
            if (nameField.getText().isBlank() || categoryCombo.getValue() == null
                    || descArea.getText().isBlank() || priceField.getText().isBlank()) {
                showAlert("Error", "Please fill all fields"); return;
            }
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                Product np = new Product(nameField.getText().trim(), categoryCombo.getValue(),
                                         descArea.getText().trim(), price, userId);
                productService.addProduct(np);
                showAlert("Success", "Product added!");
                d.close();
                callback.onProductsRefreshed(productService.getMyProducts(userId));
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid price");
            } catch (IOException ex) {
                showAlert("Error", "Failed to add product: " + ex.getMessage());
            }
        });
        cancel.setOnAction(e -> d.close());
        d.showAndWait();
    }

    // =========================================================================
    // SMALL HELPERS
    // =========================================================================

    private void populateProductCheckboxes(VBox box, List<CheckBox> checkboxes,
                                           List<Product> products, List<String> preSelected) {
        List<Product> prev  = products.stream().filter(p -> preSelected.contains(p.getId()))
                                       .collect(java.util.stream.Collectors.toList());
        List<Product> avail = products.stream().filter(p -> !preSelected.contains(p.getId()))
                                       .collect(java.util.stream.Collectors.toList());

        if (!prev.isEmpty()) {
            Label lbl = new Label("Previously Offered (Selected):");
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0275d8;");
            box.getChildren().add(lbl);
            prev.forEach(p -> {
                CheckBox cb = new CheckBox(p.getItemName() + " (₹" + p.getPrice() + ")");
                cb.setUserData(p); cb.setSelected(true); cb.setStyle("-fx-font-weight: bold;");
                box.getChildren().add(cb); checkboxes.add(cb);
            });
        }
        if (!avail.isEmpty()) {
            Label lbl = new Label("Available to Add:");
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
            box.getChildren().add(lbl);
            avail.forEach(p -> {
                CheckBox cb = new CheckBox(p.getItemName() + " (₹" + p.getPrice() + ")");
                cb.setUserData(p); cb.setSelected(false);
                box.getChildren().add(cb); checkboxes.add(cb);
            });
        }
    }

    private List<String> selectedIds(List<CheckBox> checkboxes) {
        List<String> ids = new java.util.ArrayList<>();
        checkboxes.stream().filter(CheckBox::isSelected)
            .forEach(cb -> ids.add(((Product) cb.getUserData()).getId()));
        return ids;
    }

    private double sumPrices(List<CheckBox> checkboxes) {
        return checkboxes.stream().filter(CheckBox::isSelected)
            .mapToDouble(cb -> ((Product) cb.getUserData()).getPrice()).sum();
    }

    private List<String> mergeIds(List<String> a, List<String> b) {
        java.util.Set<String> merged = new java.util.LinkedHashSet<>(a);
        merged.addAll(b);
        return new java.util.ArrayList<>(merged);
    }

    private void handleBackButton() {
        try {
            ServiceRegistry.navigationService().openBrowseProducts();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void logout() {
        sessionManager.clear();
        try { ServiceRegistry.navigationService().openLogin(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}