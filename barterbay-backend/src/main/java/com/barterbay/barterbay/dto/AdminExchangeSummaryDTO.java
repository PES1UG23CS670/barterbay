package com.barterbay.barterbay.dto;

public class AdminExchangeSummaryDTO {

    private String requester;
    private String receiver;
    private String status;

    public AdminExchangeSummaryDTO() {
        // Default constructor for serialization/deserialization.
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
