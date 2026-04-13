package com.barterbay.barterbay.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.barterbay.barterbay.model.ExchangeRequest;

public interface ExchangeRequestRepository extends MongoRepository<ExchangeRequest, String> {
    List<ExchangeRequest> findByReceiverId(String receiverId);
    List<ExchangeRequest> findByRequesterId(String requesterId);
    List<ExchangeRequest> findByReceiverIdAndStatus(String receiverId, String status);
    List<ExchangeRequest> findByRequesterIdAndStatus(String requesterId, String status);
}
