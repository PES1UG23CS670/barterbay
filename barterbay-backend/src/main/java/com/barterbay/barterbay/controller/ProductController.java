package com.barterbay.barterbay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}