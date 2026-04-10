package com.barterbay.frontend.service;

import com.barterbay.frontend.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProductService {

    private static final String BASE_URL = "http://localhost:8080/api/products";
    private final Gson gson = new Gson();

    public void addProduct(Product product) throws IOException {
        URL url = new URL(BASE_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = gson.toJson(product);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        conn.getResponseCode();
    }

    public List<Product> getMyProducts(String userId) throws IOException {
        URL url = new URL(BASE_URL + "/user/" + userId);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<Product>>(){}.getType());
    }

    public List<Product> getAllProducts() throws IOException {
        URL url = new URL(BASE_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStream is = conn.getInputStream();
        String response = new String(is.readAllBytes());

        return gson.fromJson(response,
                new TypeToken<List<Product>>(){}.getType());
    }
}