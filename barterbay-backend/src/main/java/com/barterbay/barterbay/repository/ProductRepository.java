package com.barterbay.barterbay.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.barterbay.barterbay.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
}