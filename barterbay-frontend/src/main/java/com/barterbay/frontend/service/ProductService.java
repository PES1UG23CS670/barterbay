package com.barterbay.frontend.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.barterbay.frontend.model.ExchangeRequest;
import com.barterbay.frontend.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProductService {

    private static final String BASE_URL = "http://localhost:8080/api/products";
    private final Gson gson = new Gson();

    public Product addProduct(Product product) throws IOException {
        URL url = new URL(BASE_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = gson.toJson(product);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int statusCode = conn.getResponseCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IOException("Failed to add product. Status: " + statusCode);
        }

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());
        return gson.fromJson(response, Product.class);
    }

    public List<Product> getMyProducts(String userId) throws IOException {
        URL url = new URL(BASE_URL + "/user/" + userId);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());
        
        System.out.println("DEBUG - getMyProducts response: " + response.substring(0, Math.min(500, response.length())));

        List<Product> products = gson.fromJson(response,
                new TypeToken<List<Product>>(){}.getType());
        
        for (Product p : products) {
            System.out.println("DEBUG - Product: id=" + p.getId() + ", name=" + p.getItemName());
        }
        
        return products;
    }

    public List<Product> getAllProducts() throws IOException {
        URL url = new URL(BASE_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());
        
        System.out.println("DEBUG - getAllProducts response: " + response.substring(0, Math.min(500, response.length())));

        List<Product> products = gson.fromJson(response,
                new TypeToken<List<Product>>(){}.getType());
        
        for (Product p : products) {
            System.out.println("DEBUG - Product: id=" + p.getId() + ", name=" + p.getItemName());
        }
        
        return products;
    }

    public Product getProductById(String productId) throws IOException {
        URL url = new URL(BASE_URL + "/" + productId);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response, Product.class);
    }

    public void submitExchangeRequest(String requesterId, String receiverId, String requestedProductId, java.util.List<String> offeredProductIds) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        requestMap.put("requesterId", requesterId);
        requestMap.put("receiverId", receiverId);
        requestMap.put("requestedProductId", requestedProductId);
        requestMap.put("offeredProductIds", offeredProductIds);
        
        String json = gson.toJson(requestMap);
        
        System.out.println("DEBUG - Submitting JSON: " + json);
        
        byte[] jsonBytes = json.getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBytes);
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        System.out.println("DEBUG - Response code: " + statusCode);
        
        if (statusCode != 200 && statusCode != 201) {
            String errorResponse = "";
            try (InputStream is = conn.getErrorStream()) {
                if (is != null) {
                    errorResponse = new String(is.readAllBytes());
                }
            }
            throw new IOException("Failed to submit exchange request. Status: " + statusCode + " - " + errorResponse);
        }
    }

    public List<ExchangeRequest> getPendingExchanges(String userId) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/receiver/" + userId + "/pending");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<ExchangeRequest>>(){}.getType());
    }

    public List<ExchangeRequest> getCompletedRequesterExchanges(String userId) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/requester/" + userId + "/completed");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<ExchangeRequest>>(){}.getType());
    }

    public List<ExchangeRequest> getNegotiatingExchangesForReceiver(String userId) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/receiver/" + userId + "/negotiating");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<ExchangeRequest>>(){}.getType());
    }

    public List<ExchangeRequest> getNegotiatingExchangesForRequester(String userId) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/requester/" + userId + "/negotiating");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<ExchangeRequest>>(){}.getType());
    }

    public void acceptExchange(String exchangeId) throws IOException {
        updateExchangeStatus(exchangeId, "accept");
    }

    public void rejectExchange(String exchangeId) throws IOException {
        updateExchangeStatus(exchangeId, "reject");
    }

    public void negotiateExchange(String exchangeId) throws IOException {
        updateExchangeStatus(exchangeId, "negotiate");
    }

    public void renegotiateExchange(String exchangeId, java.util.List<String> offeredProductIds) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/" + exchangeId + "/renegotiate");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        requestMap.put("offeredProductIds", offeredProductIds);
        
        String json = gson.toJson(requestMap);
        byte[] jsonBytes = json.getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBytes);
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        System.out.println("DEBUG - renegotiate Status Code: " + statusCode);
        
        if (statusCode == 200 || statusCode == 201) {
            String response = new String(conn.getInputStream().readAllBytes());
            System.out.println("DEBUG - renegotiate Response: " + response);
        } else {
            String errorResponse = "";
            try (InputStream is = conn.getErrorStream()) {
                if (is != null) {
                    errorResponse = new String(is.readAllBytes());
                }
            }
            System.out.println("DEBUG - renegotiate Error Response: " + errorResponse);
            throw new IOException("Failed to renegotiate exchange. Status: " + statusCode + " - " + errorResponse);
        }
    }

    public void updateOfferProducts(String exchangeId, java.util.List<String> offeredProductIds) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/" + exchangeId + "/update-offer");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        requestMap.put("offeredProductIds", offeredProductIds);
        
        String json = gson.toJson(requestMap);
        byte[] jsonBytes = json.getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBytes);
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        System.out.println("DEBUG - updateOffer Status Code: " + statusCode);
        
        if (statusCode == 200 || statusCode == 201) {
            String response = new String(conn.getInputStream().readAllBytes());
            System.out.println("DEBUG - updateOffer Response: " + response);
        } else {
            String errorResponse = "";
            try (InputStream is = conn.getErrorStream()) {
                if (is != null) {
                    errorResponse = new String(is.readAllBytes());
                }
            }
            System.out.println("DEBUG - updateOffer Error Response: " + errorResponse);
            throw new IOException("Failed to update offer. Status: " + statusCode + " - " + errorResponse);
        }
    }

    private void updateExchangeStatus(String exchangeId, String action) throws IOException {
        URL url = new URL("http://localhost:8080/api/exchanges/" + exchangeId + "/" + action);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes());
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        System.out.println("DEBUG - " + action + " Status Code: " + statusCode);
        
        if (statusCode == 200 || statusCode == 201) {
            String response = new String(conn.getInputStream().readAllBytes());
            System.out.println("DEBUG - " + action + " Response: " + response);
        } else {
            String errorResponse = "";
            try (InputStream is = conn.getErrorStream()) {
                if (is != null) {
                    errorResponse = new String(is.readAllBytes());
                }
            }
            System.out.println("DEBUG - " + action + " Error Response: " + errorResponse);
            throw new IOException("Failed to " + action + " exchange. Status: " + statusCode + " - " + errorResponse);
        }
    }
}