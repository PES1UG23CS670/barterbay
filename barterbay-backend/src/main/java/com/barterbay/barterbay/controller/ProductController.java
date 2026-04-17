package com.barterbay.barterbay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barterbay.barterbay.model.Product;
import com.barterbay.barterbay.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin // allow frontend calls
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {

        if (product.getUserId() == null || product.getItemName() == null) {
            throw new RuntimeException("Invalid product data");
        }

        return productRepository.save(product);
    }

    @GetMapping("/user/{userId}")
    public List<Product> getUserProducts(@PathVariable String userId) {
        return productRepository.findByUserId(userId);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}