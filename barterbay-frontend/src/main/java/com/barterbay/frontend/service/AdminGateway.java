package com.barterbay.frontend.service;

import java.io.IOException;
import java.util.List;

import com.barterbay.frontend.model.ExchangeRow;
import com.barterbay.frontend.model.UserRow;
import com.fasterxml.jackson.databind.JsonNode;

public interface AdminGateway {

    List<UserRow> getAllUsers(String role) throws IOException, InterruptedException;

    JsonNode getUserDetails(String userId, String role) throws IOException, InterruptedException;

    void updateUserStatus(String userId, String status, String role) throws IOException, InterruptedException;

    List<ExchangeRow> getExchanges(String role) throws IOException, InterruptedException;
}
