package com.barterbay.barterbay.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.barterbay.barterbay.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username); // ✅ CHECK EXISTING USER
}