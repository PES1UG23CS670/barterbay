package com.barterbay.frontend.model;

public class Product {
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
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getUserId() { return userId; }
}