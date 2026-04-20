package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.barterbay.frontend.MainApp;
import com.barterbay.frontend.filter.ByCategoryFilter;
import com.barterbay.frontend.filter.ByPriceFilter;
import com.barterbay.frontend.filter.Filter;
import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ProductService;
import com.barterbay.frontend.service.ServiceRegistry;
import com.barterbay.frontend.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

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

    // Filter UI Controls
    @FXML
    private ComboBox<String> categoryFilterCombo;
    
    @FXML
    private TextField minPriceField;
    
    @FXML
    private TextField maxPriceField;
    
    @FXML
    private Button applyFiltersBtn;
    
    @FXML
    private Button clearFiltersBtn;
    
    @FXML
    private VBox activeFilterDisplay;
    
    @FXML
    private HBox filterBadgesContainer;

    private final ProductService productService = ServiceRegistry.productService();
    private final SessionManager sessionManager = ServiceRegistry.sessionManager();
    
    // All products loaded from backend (unfiltered cache)
    private List<Product> allProducts = new ArrayList<>();
    
    // Current active filters (OCP: add new filters by creating child classes)
    private List<Filter<Product>> activeFilters = new ArrayList<>();

    // =========================================================================
    // LOAD  (GRASP Information Expert: groups products by category)
    // =========================================================================

    /**
     * Initialize the controller - loads products from backend on startup.
     */
    @FXML
    public void initialize() {
        // Setup filter controls
        setupFilterControls();
        
        // Load products from backend
        loadProductsFromBackend();
    }
    
    /**
     * Setup all filter UI controls (ComboBox, TextFields, etc.)
     */
    private void setupFilterControls() {
        // Setup price text fields with default values
        if (minPriceField != null) {
            minPriceField.setText("0");
            minPriceField.setPrefWidth(100);
            minPriceField.setStyle("-fx-padding: 6 10;");
        }
        
        if (maxPriceField != null) {
            maxPriceField.setText("100000");
            maxPriceField.setPrefWidth(100);
            maxPriceField.setStyle("-fx-padding: 6 10;");
        }
        
        // Category ComboBox will be populated after products are loaded
        if (categoryFilterCombo != null) {
            categoryFilterCombo.setPrefWidth(180);
        }
    }
    
    /**
     * Loads all products from backend API into the cache.
     */
    private void loadProductsFromBackend() {
        try {
            allProducts = productService.getAllProducts();
            
            // Populate category filter dropdown
            if (categoryFilterCombo != null && !allProducts.isEmpty()) {
                List<String> categories = allProducts.stream()
                    .map(Product::getCategory)
                    .filter(cat -> cat != null && !cat.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
                
                categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
            }
            
            refreshDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Refreshes the display with currently active filters applied.
     * OCP: new filters can be added by creating child classes of AbstractFilter
     * without modifying this method.
     */
    private void refreshDisplay() {
        try {
            productContainer.getChildren().clear();

            // Apply all active filters sequentially
            List<Product> filtered = applyFilters(allProducts);

            // Group by category for display
            Map<String, List<Product>> byCategory = groupByCategory(filtered);

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
     * Applies all active filters to the product list.
     * Each filter is independent (Strategy pattern).
     *
     * @param products initial product list
     * @return filtered product list
     */
    private List<Product> applyFilters(List<Product> products) {
        List<Product> result = new ArrayList<>(products);
        for (Filter<Product> filter : activeFilters) {
            result = filter.apply(result);
        }
        return result;
    }

    /**
     * Adds a filter to the active filters list.
     * OCP: new filter types just extend AbstractFilter<Product>
     *
     * @param filter the filter to add
     */
    public void addFilter(Filter<Product> filter) {
        if (filter != null && !activeFilters.contains(filter)) {
            activeFilters.add(filter);
        }
    }

    /**
     * Removes a filter from the active filters list.
     *
     * @param filter the filter to remove
     */
    public void removeFilter(Filter<Product> filter) {
        activeFilters.remove(filter);
    }

    /**
     * Clears all active filters and shows all products.
     */
    public void clearAllFilters() {
        activeFilters.clear();
        
        // Reset UI controls
        if (categoryFilterCombo != null) {
            categoryFilterCombo.setValue(null);
        }
        if (minPriceField != null) {
            minPriceField.setText("0");
        }
        if (maxPriceField != null) {
            maxPriceField.setText("100000");
        }
        
        // Hide active filter display
        if (activeFilterDisplay != null) {
            activeFilterDisplay.setVisible(false);
            activeFilterDisplay.setManaged(false);
        }
        
        refreshDisplay();
    }
    
    /**
     * Updates the active filter display panel.
     * Shows badges for each active filter.
     */
    private void updateFilterDisplay() {
        if (filterBadgesContainer == null) return;
        
        filterBadgesContainer.getChildren().clear();
        
        if (activeFilters.isEmpty()) {
            if (activeFilterDisplay != null) {
                activeFilterDisplay.setVisible(false);
                activeFilterDisplay.setManaged(false);
            }
            return;
        }
        
        // Show active filter display
        if (activeFilterDisplay != null) {
            activeFilterDisplay.setVisible(true);
            activeFilterDisplay.setManaged(true);
        }
        
        // Create badge labels for each filter
        for (Filter<Product> filter : activeFilters) {
            Label badge = new Label(filter.getFilterName());
            badge.setStyle("-fx-padding: 6 12; -fx-background-color: #3b82f6; -fx-text-fill: white; "
                         + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 10;");
            filterBadgesContainer.getChildren().add(badge);
        }
    }

    /**
     * UI Action: Apply filters from the filter controls.
     * Called when user clicks "Apply Filters" button.
     * 
     * When category is selected:
     * - Only products from that category are shown (other categories disappear)
     * - Price filter is applied on top if specified
     */
    @FXML
    public void applyFilters() {
        try {
            activeFilters.clear();
            
            String selectedCategory = categoryFilterCombo != null ? categoryFilterCombo.getValue() : null;
            String minPriceText = minPriceField != null ? minPriceField.getText().trim() : "0";
            String maxPriceText = maxPriceField != null ? maxPriceField.getText().trim() : "100000";
            
            // Validate and parse prices
            Double minPrice = 0.0;
            Double maxPrice = 100000.0;
            
            try {
                if (!minPriceText.isEmpty()) {
                    minPrice = Double.parseDouble(minPriceText);
                }
                if (!maxPriceText.isEmpty()) {
                    maxPrice = Double.parseDouble(maxPriceText);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Price", "Please enter valid numbers for price range.");
                return;
            }
            
            // Apply category filter if selected
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                addFilter(new ByCategoryFilter(selectedCategory));
            }
            
            // Apply price filter if values are valid
            if (minPrice >= 0 && maxPrice >= minPrice) {
                addFilter(new ByPriceFilter(minPrice, maxPrice));
            }
            
            // Update filter display badges
            updateFilterDisplay();
            
            // Refresh products with active filters
            refreshDisplay();
            
            if (!activeFilters.isEmpty()) {
                showAlert("Success", "Filters applied successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to apply filters: " + e.getMessage());
        }
    }

    /**
     * Applies a category filter (OCP: extending AbstractFilter<Product>).
     *
     * @param category the category to filter by
     */
    public void filterByCategory(String category) {
        clearAllFilters();
        if (category != null && !category.isEmpty()) {
            addFilter(new ByCategoryFilter(category));
            refreshDisplay();
        }
    }

    /**
     * Applies a price range filter (OCP: extending AbstractFilter<Product>).
     *
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     */
    public void filterByPrice(double minPrice, double maxPrice) {
        clearAllFilters();
        if (minPrice >= 0 && maxPrice >= minPrice) {
            addFilter(new ByPriceFilter(minPrice, maxPrice));
            refreshDisplay();
        }
    }

    /**
     * Applies both category and price filters simultaneously.
     * Demonstrates filter composition with OCP.
     *
     * @param category the category to filter by
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     */
    public void filterByCategoryAndPrice(String category, double minPrice, double maxPrice) {
        clearAllFilters();
        if (category != null && !category.isEmpty()) {
            addFilter(new ByCategoryFilter(category));
        }
        if (minPrice >= 0 && maxPrice >= minPrice) {
            addFilter(new ByPriceFilter(minPrice, maxPrice));
        }
        refreshDisplay();
    }

    // =========================================================================
    // CATEGORIZATION (GRASP Information Expert)
    // =========================================================================

    /**
     * Groups valid products by category, preserving insertion order.
     * "Other" is used as a fallback category if the field is null/blank.
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
