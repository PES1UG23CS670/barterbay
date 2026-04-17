package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   This controller loads products and groups them by category for display.
 *   No exchange logic lives here.
 *
 * SOLID – Open/Closed Principle (OCP)
 *   New categories appear automatically; no code change needed when a new
 *   category is added to the backend.
 *
 * GRASP – Information Expert
 *   groupByCategory() is the right place to know how products are organised
 *   because this controller owns the product list.
 *
 * CHANGE vs original:
 *   loadProducts() now calls groupByCategory() and renders one labeled
 *   section + FlowPane per category instead of a single flat FlowPane.
 *   The FXML center node changes from FlowPane → ScrollPane > VBox (productContainer).
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/controller/BrowseProductsController.java
 *   (replaces the existing file)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class BrowseProductsController {

    /**
     * Changed from FlowPane to VBox so we can stack category sections.
     * Matching FXML change: fx:id="productContainer" is now a VBox.
     */
    @FXML
    private VBox productContainer;

    @FXML
    private Button logoutBtn;

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();

    @FXML
    public void initialize() {
        loadProducts();
    }

    // =========================================================================
    // LOAD  (GRASP Information Expert: groups products by category)
    // =========================================================================

    private void loadProducts() {
        try {
            productContainer.getChildren().clear();

            List<Product> products = productService.getAllProducts();

            // GRASP Information Expert: categorisation logic lives here
            Map<String, List<Product>> byCategory = groupByCategory(products);

            if (byCategory.isEmpty()) {
                Label empty = new Label("No products available.");
                empty.setStyle("-fx-font-size: 13; -fx-text-fill: #999;");
                productContainer.getChildren().add(empty);
                return;
            }

            // SOLID OCP: adding a new category requires no code change here
            for (Map.Entry<String, List<Product>> entry : byCategory.entrySet()) {
                productContainer.getChildren().add(buildCategorySection(entry.getKey(), entry.getValue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Groups valid products by category, preserving insertion order.
     * "Unknown" is used as a fallback category if the field is null/blank.
     *
     * GRASP – Information Expert: only BrowseProductsController needs this grouping.
     */
    private Map<String, List<Product>> groupByCategory(List<Product> products) {
        Map<String, List<Product>> map = new LinkedHashMap<>();
        for (Product p : products) {
            if (p.getUserId() == null || p.getItemName() == null) continue; // skip invalid
            String cat = (p.getCategory() == null || p.getCategory().isBlank())
                    ? "Other" : capitalise(p.getCategory());
            map.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
        }
        return map;
    }

    // =========================================================================
    // UI BUILDERS
    // =========================================================================

    /**
     * Builds one category section: a bold header label + a FlowPane of cards.
     *
     * SOLID SRP: each section is self-contained; the controller just stacks them.
     */
    private VBox buildCategorySection(String categoryName, List<Product> products) {
        VBox section = new VBox(10);
        section.setStyle("-fx-padding: 10 20 6 20;");

        // Category header
        Label header = new Label(categoryName);
        header.setStyle(
            "-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #374151;" +
            "-fx-border-color: transparent transparent #e5e7eb transparent;" +
            "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 6 0;"
        );

        // Cards in a wrapping FlowPane (same layout as original single pane)
        FlowPane flow = new FlowPane();
        flow.setHgap(20);
        flow.setVgap(20);
        flow.setPrefWrapLength(900);

        for (Product p : products) {
            flow.getChildren().add(createCard(p));
        }

        section.getChildren().addAll(header, flow);
        return section;
    }

    /** Card building is UNCHANGED from the original. */
    private VBox createCard(Product p) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label name  = new Label(p.getItemName());
        name.getStyleClass().add("title");

        Label price = new Label("₹" + p.getPrice());

        Label desc  = new Label(p.getDescription());
        desc.getStyleClass().add("muted");

        card.getChildren().addAll(name, price, desc);
        card.setOnMouseClicked(e -> showProductPopup(p));

        return card;
    }

    /** Product popup is UNCHANGED from the original. */
    private void showProductPopup(Product p) {
        VBox layout = new VBox(12);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        layout.setPrefWidth(300);

        Label name     = new Label(p.getItemName());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        Label category = new Label("Category: " + p.getCategory());
        Label price    = new Label("Price: ₹" + p.getPrice());
        Label desc     = new Label(p.getDescription());
        desc.setWrapText(true);

        Button exchangeBtn = new Button("Exchange");

        Scene scene = new Scene(layout);
        Stage popup = new Stage();

        exchangeBtn.setOnAction(e -> {
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
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.showAndWait();
    }

    // =========================================================================
    // NAVIGATION  (unchanged)
    // =========================================================================

    @FXML public void goToBrowse()    { /* already here */ }

    @FXML public void goToListings()  throws IOException {
        ServiceRegistry.navigationService().openMyListings();
    }

    @FXML public void goToExchanges() throws IOException {
        ServiceRegistry.navigationService().openExchanges();
    }

    @FXML
    public void logout() {
        sessionManager.clear();
        try { ServiceRegistry.navigationService().openLogin(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private String capitalise(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
