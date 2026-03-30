package com.barterbay.frontend.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ApiService {

    public boolean signup(String username, String password) throws IOException {

        URL url = URI.create("http://localhost:8080/api/users/signup?username="
            + username + "&password=" + password).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        return conn.getResponseCode() == 200;
    }
}