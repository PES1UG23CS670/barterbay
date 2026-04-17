package com.barterbay.frontend.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barterbay.frontend.model.ExchangeRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   This class is ONLY responsible for exchange HTTP calls.
 *   Previously, ProductService mixed product AND exchange HTTP logic in one
 *   class.  That mix is now separated.
 *
 * SOLID – Open/Closed Principle (OCP)
 *   New exchange endpoints can be added by implementing a new method without
 *   modifying existing ones.
 *
 * GRASP – Low Coupling
 *   Uses private helper methods (put, get) to eliminate repeated boilerplate.
 *   Controllers only see the ExchangeGateway interface.
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/service/ExchangeGatewayImpl.java
 *   (NEW file – add to the project)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ExchangeGatewayImpl implements ExchangeGateway {

    private static final String BASE = "http://localhost:8080/api/exchanges";
    private final Gson gson = new Gson();

    // =========================================================================
    // ExchangeGateway implementation
    // =========================================================================

    @Override
    public void submit(String requesterId, String receiverId,
                       String requestedProductId, List<String> offeredProductIds) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("requesterId",       requesterId);
        body.put("receiverId",        receiverId);
        body.put("requestedProductId", requestedProductId);
        body.put("offeredProductIds",  offeredProductIds);
        postVoid(BASE, body);
    }

    @Override
    public List<ExchangeRequest> getPendingForReceiver(String userId) throws IOException {
        return getList(BASE + "/receiver/" + userId + "/pending");
    }

    @Override
    public List<ExchangeRequest> getNegotiatingForReceiver(String userId) throws IOException {
        return getList(BASE + "/receiver/" + userId + "/negotiating");
    }

    @Override
    public List<ExchangeRequest> getNegotiatingForRequester(String userId) throws IOException {
        return getList(BASE + "/requester/" + userId + "/negotiating");
    }

    @Override
    public List<ExchangeRequest> getCompletedForRequester(String userId) throws IOException {
        return getList(BASE + "/requester/" + userId + "/completed");
    }

    @Override
    public void accept(String exchangeId) throws IOException {
        putVoid(BASE + "/" + exchangeId + "/accept");
    }

    @Override
    public void reject(String exchangeId) throws IOException {
        putVoid(BASE + "/" + exchangeId + "/reject");
    }

    @Override
    public void negotiate(String exchangeId) throws IOException {
        putVoid(BASE + "/" + exchangeId + "/negotiate");
    }

    @Override
    public void renegotiate(String exchangeId, List<String> offeredProductIds) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("offeredProductIds", offeredProductIds);
        putWithBody(BASE + "/" + exchangeId + "/renegotiate", body);
    }

    @Override
    public void updateOffer(String exchangeId, List<String> offeredProductIds) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("offeredProductIds", offeredProductIds);
        putWithBody(BASE + "/" + exchangeId + "/update-offer", body);
    }

    // =========================================================================
    // PRIVATE HTTP HELPERS  (GRASP: Low Coupling / DRY)
    // =========================================================================

    private List<ExchangeRequest> getList(String endpoint) throws IOException {
        HttpURLConnection conn = open(endpoint, "GET");
        requireSuccess(conn, "GET " + endpoint);
        String json = new String(conn.getInputStream().readAllBytes());
        return gson.fromJson(json, new TypeToken<List<ExchangeRequest>>(){}.getType());
    }

    private void postVoid(String endpoint, Object body) throws IOException {
        HttpURLConnection conn = open(endpoint, "POST");
        conn.setDoOutput(true);
        writeBody(conn, body);
        requireSuccess(conn, "POST " + endpoint);
    }

    private void putVoid(String endpoint) throws IOException {
        HttpURLConnection conn = open(endpoint, "PUT");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes());
        }
        requireSuccess(conn, "PUT " + endpoint);
    }

    private void putWithBody(String endpoint, Object body) throws IOException {
        HttpURLConnection conn = open(endpoint, "PUT");
        conn.setDoOutput(true);
        writeBody(conn, body);
        requireSuccess(conn, "PUT " + endpoint);
    }

    private HttpURLConnection open(String endpoint, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept",       "application/json");
        conn.setDoInput(true);
        return conn;
    }

    private void writeBody(HttpURLConnection conn, Object body) throws IOException {
        byte[] bytes = gson.toJson(body).getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }
    }

    private void requireSuccess(HttpURLConnection conn, String label) throws IOException {
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            String err = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) err = new String(es.readAllBytes());
            }
            throw new IOException("HTTP " + code + " on " + label + ": " + err);
        }
    }
}