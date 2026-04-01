package com.barterbay.frontend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.barterbay.frontend.model.ExchangeRow;
import com.barterbay.frontend.model.UserRow;
import com.barterbay.frontend.model.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiService implements AuthGateway, AdminGateway {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String STATUS = "status";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean signup(String username, String password) throws IOException, InterruptedException {
        String json = credentialsJson(username, password);

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/users/signup"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return true;
        }
        throw new IOException(extractError(response.body(), response.statusCode()));
    }

    @Override
    public UserSession login(String username, String password) throws IOException, InterruptedException {
        String json = credentialsJson(username, password);

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(extractError(response.body(), response.statusCode()));
        }

        JsonNode body = objectMapper.readTree(response.body());
        return new UserSession(
                text(body, "id", "_id"),
                text(body, "username"),
                text(body, "role"),
                text(body, STATUS));
    }

    @Override
    public List<UserRow> getAllUsers(String role) throws IOException, InterruptedException {
        String endpoint = String.format("%s/api/admin/users?role=%s", BASE_URL, encode(role));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(extractError(response.body(), response.statusCode()));
        }

        JsonNode array = objectMapper.readTree(response.body());
        List<UserRow> users = new ArrayList<>();
        for (JsonNode node : array) {
            users.add(new UserRow(
                    text(node, "id", "_id"),
                    text(node, "username"),
                    text(node, "role"),
                    text(node, STATUS)));
        }
        return users;
    }

    @Override
    public JsonNode getUserDetails(String userId, String role) throws IOException, InterruptedException {
        String endpoint = String.format("%s/api/admin/users/%s/details?role=%s",
                BASE_URL,
                encode(userId),
                encode(role));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(extractError(response.body(), response.statusCode()));
        }
        return objectMapper.readTree(response.body());
    }

    @Override
    public void updateUserStatus(String userId, String status, String role) throws IOException, InterruptedException {
        String endpoint = String.format("%s/api/admin/users/%s/status?status=%s&role=%s",
                BASE_URL,
                encode(userId),
                encode(status),
                encode(role));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(extractError(response.body(), response.statusCode()));
        }
    }

    @Override
    public List<ExchangeRow> getExchanges(String role) throws IOException, InterruptedException {
        String endpoint = String.format("%s/api/admin/exchanges?role=%s", BASE_URL, encode(role));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(extractError(response.body(), response.statusCode()));
        }

        JsonNode array = objectMapper.readTree(response.body());
        List<ExchangeRow> exchanges = new ArrayList<>();
        for (JsonNode node : array) {
            exchanges.add(new ExchangeRow(
                    text(node, "requester"),
                    text(node, "receiver"),
                    text(node, STATUS)));
        }
        return exchanges;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.get(key);
            if (child != null && !child.isNull()) {
                return child.asText();
            }
        }
        return "";
    }

    private String credentialsJson(String username, String password) throws JsonProcessingException {
        JsonNode payload = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password);
        return objectMapper.writeValueAsString(payload);
    }

    private String extractError(String body, int statusCode) {
        if (body == null || body.isBlank()) {
            return "Request failed with status " + statusCode;
        }
        try {
            JsonNode json = objectMapper.readTree(body);
            String message = text(json, "message", "error");
            if (!message.isBlank()) {
                return message;
            }
        } catch (IOException ignored) {
            // Keep original body if response is plain text.
        }
        return body;
    }
}