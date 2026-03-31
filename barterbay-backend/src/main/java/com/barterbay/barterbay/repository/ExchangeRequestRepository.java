package com.barterbay.barterbay.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.barterbay.barterbay.model.ExchangeRequest;

public interface ExchangeRequestRepository extends MongoRepository<ExchangeRequest, String> {
}
