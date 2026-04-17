package com.barterbay.barterbay.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "exchange_requests")
public class ExchangeRequest {

    @Id
    private String id;

    private String requesterId;

    private String receiverId;

    private String requestedProductId;

    private List<String> offeredProductIds;

    private String status;

    private int negotiationCount;

    public ExchangeRequest() {
        // Default constructor for Spring Data mapping.
        this.negotiationCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getRequestedProductId() {
        return requestedProductId;
    }

    public void setRequestedProductId(String requestedProductId) {
        this.requestedProductId = requestedProductId;
    }

    public List<String> getOfferedProductIds() {
        return offeredProductIds;
    }

    public void setOfferedProductIds(List<String> offeredProductIds) {
        this.offeredProductIds = offeredProductIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNegotiationCount() {
        return negotiationCount;
    }

    public void setNegotiationCount(int negotiationCount) {
        this.negotiationCount = negotiationCount;
    }
}
