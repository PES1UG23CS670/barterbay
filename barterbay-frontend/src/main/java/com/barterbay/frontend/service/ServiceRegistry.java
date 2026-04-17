package com.barterbay.frontend.service;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Dependency-Inversion Principle (DIP)
 *   Controllers receive interfaces (ExchangeGateway, not ExchangeGatewayImpl).
 *
 * GRASP – Low Coupling
 *   All wiring is in ONE place; swapping an implementation only changes here.
 *
 * CHANGE vs original:
 *   Added exchangeGateway() factory method.
 *   ProductService exchange methods are no longer called from controllers –
 *   those calls are now routed through ExchangeGateway.
 *   ProductService itself is UNCHANGED (still handles product HTTP calls).
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/service/ServiceRegistry.java
 *   (replaces the existing file)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public final class ServiceRegistry {

    private static final ApiService          API_SERVICE          = new ApiService();
    private static final SessionManager      SESSION_MANAGER      = new SessionManager();
    private static final NavigationService   NAVIGATION_SERVICE   = new NavigationService();
    private static final ProductService      PRODUCT_SERVICE      = new ProductService();

    // ── NEW: ExchangeGateway wired to its implementation ─────────────────────
    private static final ExchangeGateway EXCHANGE_GATEWAY = new ExchangeGatewayImpl();

    private ServiceRegistry() { /* utility class */ }

    public static AuthGateway authGateway()               { return API_SERVICE;        }
    public static AdminGateway adminGateway()             { return API_SERVICE;        }
    public static SessionManager sessionManager()         { return SESSION_MANAGER;    }
    public static NavigationService navigationService()   { return NAVIGATION_SERVICE; }
    public static ProductService productService()         { return PRODUCT_SERVICE;    }

    /**
     * Returns the ExchangeGateway interface – controllers depend on the
     * interface, never on ExchangeGatewayImpl directly.
     */
    public static ExchangeGateway exchangeGateway()       { return EXCHANGE_GATEWAY;  }
}