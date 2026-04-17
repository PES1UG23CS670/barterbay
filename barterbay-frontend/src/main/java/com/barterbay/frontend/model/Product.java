package com.barterbay.frontend.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private String id;
    private String itemName;
    private String category;
    private String description;
    private double price;
    private String userId;

    public Product(String itemName, String category, String description, double price, String userId) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.userId = userId;
    }
    
    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}