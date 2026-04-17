package com.barterbay.barterbay.service;

import java.util.List;

import com.barterbay.barterbay.model.ExchangeRequest;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Dependency-Inversion Principle (DIP)
 *   ExchangeController now depends on THIS abstraction, not on repositories
 *   directly.  Swap implementations (e.g. for testing) without touching the
 *   controller.
 *
 * SOLID – Interface-Segregation Principle (ISP)
 *   Only exchange-domain operations are declared here; product-lookup helpers
 *   are handled by the implementation internally and not exposed.
 *
 * GRASP – Information Expert
 *   The class that knows about exchange rules (price validation, negotiation
 *   limits) is the one that enforces them – not the controller.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface ExchangeService {

    ExchangeRequest create(ExchangeRequest request);

    ExchangeRequest findById(String id);

    List<ExchangeRequest> findAll();

    ExchangeRequest updateStatus(String id, String status);

    void delete(String id);

    // ── receiver queries ──────────────────────────────────────────────────────
    List<ExchangeRequest> findByReceiver(String receiverId);
    List<ExchangeRequest> findPendingByReceiver(String receiverId);
    List<ExchangeRequest> findNegotiatingByReceiver(String receiverId);

    // ── requester queries ─────────────────────────────────────────────────────
    List<ExchangeRequest> findByRequester(String requesterId);
    List<ExchangeRequest> findCompletedByRequester(String requesterId);
    List<ExchangeRequest> findNegotiatingByRequester(String requesterId);

    // ── lifecycle actions ─────────────────────────────────────────────────────
    ExchangeRequest accept(String id);
    ExchangeRequest reject(String id);
    ExchangeRequest negotiate(String id);
    ExchangeRequest renegotiate(String id, List<String> newOfferedProductIds);
    ExchangeRequest updateOffer(String id, List<String> newOfferedProductIds);
}