package com.barterbay.frontend.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.barterbay.frontend.model.ExchangeRequest;
import com.barterbay.frontend.model.Product;
import com.barterbay.frontend.service.ExchangeGateway;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   This class has ONE job: build an exchange VBox card for the UI.
 *   Previously, all card-building logic was inlined inside ExchangesController,
 *   making that controller a 400+ line god-class.
 *
 * SOLID – Open/Closed Principle (OCP)
 *   Add a new card section by adding a new private method and wiring it in
 *   buildCard() – no need to touch ExchangesController.
 *
 * GRASP – Creator
 *   ExchangeCardFactory is the natural creator of exchange VBox cards because
 *   it has all the information required (exchange, products, callbacks).
 *
 * GRASP – Low Coupling
 *   ExchangesController no longer contains card-building code.
 *   It only calls factory.buildCard(...) and appends the result.
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/controller/ExchangeCardFactory.java
 *   (NEW file – add to the project)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ExchangeCardFactory {

    private final ExchangeGateway exchangeGateway;
    private final Map<String, Product> productCache;
    private final Runnable onRefresh;   // called after any action to reload the list

    /**
     * @param exchangeGateway  injected interface (DIP)
     * @param productCache     shared cache maintained by the controller
     * @param onRefresh        callback to reload exchanges in the parent view
     */
    public ExchangeCardFactory(ExchangeGateway exchangeGateway,
                                Map<String, Product> productCache,
                                Runnable onRefresh) {
        this.exchangeGateway = exchangeGateway;
        this.productCache    = productCache;
        this.onRefresh       = onRefresh;
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Builds and returns the full card VBox for one exchange.
     * isReceiver controls which action buttons are shown.
     */
    public VBox buildCard(ExchangeRequest exchange, boolean isReceiver) {
        VBox card = new VBox(12);
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 16;" +
                      "-fx-border-radius: 4; -fx-background-color: #f9fafb;");
        card.setPrefWidth(Double.MAX_VALUE);

        card.getChildren().add(buildHeader(exchange, isReceiver));
        card.getChildren().add(buildDetails(exchange));
        buildActionSection(exchange, isReceiver).ifPresent(card.getChildren()::add);

        return card;
    }

    // =========================================================================
    // PRIVATE BUILDERS
    // =========================================================================

    private HBox buildHeader(ExchangeRequest exchange, boolean isReceiver) {
        HBox header = new HBox(10);
        String userLabel = isReceiver ? "From Requester: " : "To User: ";
        String userId    = isReceiver ? exchange.getRequesterId() : exchange.getReceiverId();

        Label userIdLabel = new Label(userLabel + userId.substring(0, 8) + "...");
        userIdLabel.setStyle("-fx-font-weight: bold;");

        Label statusLabel = new Label("Status: " + exchange.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + statusColor(exchange.getStatus()) + "; -fx-font-weight: bold;");

        header.getChildren().addAll(userIdLabel, statusLabel);
        return header;
    }

    private VBox buildDetails(ExchangeRequest exchange) {
        VBox details = new VBox(8);

        Label requestedTitle = new Label("Requested Product:");
        requestedTitle.setStyle("-fx-font-weight: bold;");

        Product requestedProduct = cachedProduct(exchange.getRequestedProductId());
        String  requestedName    = requestedProduct != null ? requestedProduct.getItemName() : "Unknown";
        double  requestedPrice   = requestedProduct != null ? requestedProduct.getPrice() : 0;

        Label requestedLabel = new Label(requestedName + " (₹" + requestedPrice + ")");
        requestedLabel.setStyle("-fx-text-fill: #333;");

        Label offeringTitle = new Label("Offered Products:");
        offeringTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        VBox offeredBox = buildOfferedProducts(exchange.getOfferedProductIds());

        details.getChildren().addAll(requestedTitle, requestedLabel, offeringTitle, offeredBox);
        return details;
    }

    private VBox buildOfferedProducts(List<String> offeredIds) {
        VBox box = new VBox(4);
        double total = 0;
        if (offeredIds != null && !offeredIds.isEmpty()) {
            for (String id : offeredIds) {
                Product p     = cachedProduct(id);
                String  name  = p != null ? p.getItemName() : "Unknown";
                double  price = p != null ? p.getPrice() : 0;
                total += price;
                box.getChildren().add(new Label("  • " + name + " (₹" + price + ")"));
            }
        } else {
            box.getChildren().add(new Label("  No products offered"));
        }
        Label totalLabel = new Label("  Total Value: ₹" + total);
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
        box.getChildren().add(totalLabel);
        return box;
    }

    /**
     * SOLID – OCP: new action sections can be added here without changing
     * the caller (ExchangesController).
     */
    private java.util.Optional<HBox> buildActionSection(ExchangeRequest exchange, boolean isReceiver) {
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        if (isReceiver) {
            addReceiverButtons(exchange, buttons);
        } else if ("PENDING".equals(exchange.getStatus())) {
            addModifyButton(exchange, buttons);
        } else if ("NEGOTIATING".equals(exchange.getStatus()) && exchange.getNegotiationCount() < 2) {
            addRenegotiateButton(exchange, buttons);
        } else if ("NEGOTIATING".equals(exchange.getStatus()) && exchange.getNegotiationCount() >= 2) {
            addMaxReachedLabel(exchange, buttons);
        } else {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(buttons);
    }

    private void addReceiverButtons(ExchangeRequest exchange, HBox container) {
        Button accept = styledBtn("Accept", "-fx-padding: 8 20; -fx-font-size: 12;");
        accept.setOnAction(e -> runAction(() -> {
            exchangeGateway.accept(exchange.getId());
            showAlert("Success", "Exchange accepted!");
        }));

        Button reject = styledBtn("Reject",
            "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #dc3545; -fx-text-fill: white;");
        reject.setOnAction(e -> runAction(() -> {
            exchangeGateway.reject(exchange.getId());
            showAlert("Success", "Exchange rejected!");
        }));

        Button negotiate = styledBtn(
            exchange.getNegotiationCount() >= 2 ? "Negotiate (Max reached)" : "Negotiate",
            "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #ffc107; -fx-text-fill: black;");
        negotiate.setDisable(exchange.getNegotiationCount() >= 2);
        negotiate.setOnAction(e -> runAction(() -> {
            exchangeGateway.negotiate(exchange.getId());
            showAlert("Success", "Negotiation started!");
        }));

        Label negInfo = new Label("Negotiation attempt: " + exchange.getNegotiationCount() + "/2");
        negInfo.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");

        container.getChildren().addAll(accept, reject, negotiate, negInfo);
    }

    private void addModifyButton(ExchangeRequest exchange, HBox container) {
        Button modify = styledBtn("Modify Offer",
            "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #17a2b8; -fx-text-fill: white;");
        modify.setOnAction(e -> onRefresh.run()); // hook: controller opens dialog
        container.getChildren().add(modify);
    }

    private void addRenegotiateButton(ExchangeRequest exchange, HBox container) {
        Button btn = styledBtn("Modify & Send",
            "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #007bff; -fx-text-fill: white;");
        btn.setOnAction(e -> onRefresh.run());
        Label info = new Label("Negotiation attempt: " + exchange.getNegotiationCount() + "/2");
        info.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        container.getChildren().addAll(btn, info);
    }

    private void addMaxReachedLabel(ExchangeRequest exchange, HBox container) {
        Label label = new Label("Waiting for receiver (Max negotiations reached: "
            + exchange.getNegotiationCount() + "/2)");
        label.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
        container.getChildren().add(label);
    }

    // =========================================================================
    // SMALL HELPERS
    // =========================================================================

    private Button styledBtn(String text, String style) {
        Button b = new Button(text);
        b.setStyle(style);
        return b;
    }

    private Product cachedProduct(String id) {
        return productCache.get(id);
    }

    private String statusColor(String status) {
        switch (status) {
            case "ACCEPTED":    return "#28a745";
            case "REJECTED":    return "#dc3545";
            case "NEGOTIATING": return "#ffc107";
            default:            return "#666";
        }
    }

    /**
     * Wraps an IOException-throwing action, shows an error alert on failure,
     * and calls onRefresh on success.
     */
    private void runAction(IOAction action) {
        try {
            action.run();
            onRefresh.run();
        } catch (IOException e) {
            showAlert("Error", "Action failed: " + e.getMessage());
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
    private interface IOAction { void run() throws IOException; }
}