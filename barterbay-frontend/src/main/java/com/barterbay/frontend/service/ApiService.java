package com.barterbay.frontend.service;

import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    public boolean signup(String username, String password) throws Exception {

        URL url = new URL("http://localhost:8080/api/users/signup?username="
                + username + "&password=" + password);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        return conn.getResponseCode() == 200;
    }
}