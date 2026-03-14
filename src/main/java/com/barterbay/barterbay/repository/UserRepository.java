package com.barterbay.barterbay.repository;

import com.barterbay.barterbay.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

}