package com.barterbay.barterbay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.barterbay.barterbay.model.ExchangeRequest;
import com.barterbay.barterbay.service.ExchangeService;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   This controller is ONLY responsible for HTTP mapping: parsing requests and
 *   returning HTTP responses.  All business logic has moved to ExchangeService.
 *
 * SOLID – Dependency-Inversion Principle (DIP)
 *   Depends on the ExchangeService INTERFACE, not on ExchangeServiceImpl.
 *   A mock/stub can be injected in tests without any change here.
 *
 * GRASP – Low Coupling
 *   No direct dependency on ExchangeRequestRepository or ProductRepository.
 *   The controller only knows about one collaborator: ExchangeService.
 *
 * GRASP – Controller (GRASP pattern)
 *   Acts as the system boundary; receives HTTP events and delegates to the
 *   domain service.
 *
 * NOTE: existing URL routes, HTTP methods, and response shapes are UNCHANGED.
 *       Only internal delegation has changed (was: direct repo calls → now:
 *       ExchangeService calls).
 *
 * WHERE THIS FILE GOES:
 *   barterbay-backend/src/main/java/com/barterbay/barterbay/controller/ExchangeController.java
 *   (replaces the existing file)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/exchanges")
@CrossOrigin
public class ExchangeController {

    // ── DIP: depend on abstraction ────────────────────────────────────────────
    private final ExchangeService exchangeService;

    @Autowired
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    // =========================================================================
    // CRUD  (routes unchanged)
    // =========================================================================

    @PostMapping
    public ResponseEntity<ExchangeRequest> createExchangeRequest(@RequestBody ExchangeRequest request) {
        // SRP: no validation logic here – service handles it
        ExchangeRequest saved = exchangeService.create(request);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRequest> getExchangeRequest(@PathVariable String id) {
        return ResponseEntity.ok(exchangeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRequest>> getAllExchangeRequests() {
        return ResponseEntity.ok(exchangeService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRequest> updateExchangeRequest(
            @PathVariable String id,
            @RequestBody ExchangeRequest request) {
        return ResponseEntity.ok(exchangeService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeRequest(@PathVariable String id) {
        exchangeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // RECEIVER QUERIES  (routes unchanged)
    // =========================================================================

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<ExchangeRequest>> getExchangesForReceiver(@PathVariable String receiverId) {
        return ResponseEntity.ok(exchangeService.findByReceiver(receiverId));
    }

    @GetMapping("/receiver/{receiverId}/pending")
    public ResponseEntity<List<ExchangeRequest>> getPendingExchangesForReceiver(@PathVariable String receiverId) {
        return ResponseEntity.ok(exchangeService.findPendingByReceiver(receiverId));
    }

    @GetMapping("/receiver/{receiverId}/negotiating")
    public ResponseEntity<List<ExchangeRequest>> getNegotiatingExchangesForReceiver(@PathVariable String receiverId) {
        return ResponseEntity.ok(exchangeService.findNegotiatingByReceiver(receiverId));
    }

    // =========================================================================
    // REQUESTER QUERIES  (routes unchanged)
    // =========================================================================

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ExchangeRequest>> getExchangesForRequester(@PathVariable String requesterId) {
        return ResponseEntity.ok(exchangeService.findByRequester(requesterId));
    }

    @GetMapping("/requester/{requesterId}/completed")
    public ResponseEntity<List<ExchangeRequest>> getCompletedExchangesForRequester(@PathVariable String requesterId) {
        return ResponseEntity.ok(exchangeService.findCompletedByRequester(requesterId));
    }

    @GetMapping("/requester/{requesterId}/negotiating")
    public ResponseEntity<List<ExchangeRequest>> getNegotiatingExchangesForRequester(@PathVariable String requesterId) {
        return ResponseEntity.ok(exchangeService.findNegotiatingByRequester(requesterId));
    }

    // =========================================================================
    // LIFECYCLE ACTIONS  (routes unchanged)
    // =========================================================================

    @PutMapping("/{id}/accept")
    public ResponseEntity<ExchangeRequest> acceptExchange(@PathVariable String id) {
        return ResponseEntity.ok(exchangeService.accept(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ExchangeRequest> rejectExchange(@PathVariable String id) {
        return ResponseEntity.ok(exchangeService.reject(id));
    }

    @PutMapping("/{id}/negotiate")
    public ResponseEntity<ExchangeRequest> negotiateExchange(@PathVariable String id) {
        return ResponseEntity.ok(exchangeService.negotiate(id));
    }

    @PutMapping("/{id}/renegotiate")
    public ResponseEntity<ExchangeRequest> renegotiateExchange(
            @PathVariable String id,
            @RequestBody ExchangeRequest request) {
        return ResponseEntity.ok(exchangeService.renegotiate(id, request.getOfferedProductIds()));
    }

    @PutMapping("/{id}/update-offer")
    public ResponseEntity<ExchangeRequest> updateOfferProducts(
            @PathVariable String id,
            @RequestBody ExchangeRequest request) {
        return ResponseEntity.ok(exchangeService.updateOffer(id, request.getOfferedProductIds()));
    }
}