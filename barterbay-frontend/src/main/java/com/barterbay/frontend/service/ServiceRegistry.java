package com.barterbay.frontend.service;

public final class ServiceRegistry {

    private static final ApiService API_SERVICE = new ApiService();
    private static final SessionManager SESSION_MANAGER = new SessionManager();
    private static final NavigationService NAVIGATION_SERVICE = new NavigationService();

    private ServiceRegistry() {
        // Utility class.
    }

    public static AuthGateway authGateway() {
        return API_SERVICE;
    }

    public static AdminGateway adminGateway() {
        return API_SERVICE;
    }

    public static SessionManager sessionManager() {
        return SESSION_MANAGER;
    }

    public static NavigationService navigationService() {
        return NAVIGATION_SERVICE;
    }
}
