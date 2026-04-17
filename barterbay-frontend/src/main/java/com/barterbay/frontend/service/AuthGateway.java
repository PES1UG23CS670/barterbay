package com.barterbay.frontend.service;

import java.io.IOException;

import com.barterbay.frontend.model.UserSession;

public interface AuthGateway {

    boolean signup(String username, String password) throws IOException, InterruptedException;

    UserSession login(String username, String password) throws IOException, InterruptedException;
}
