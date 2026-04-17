package com.barterbay.barterbay.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barterbay.barterbay.exception.BadRequestException;
import com.barterbay.barterbay.exception.NotFoundException;
import com.barterbay.barterbay.model.ExchangeRequest;
import com.barterbay.barterbay.model.Product;
import com.barterbay.barterbay.repository.ExchangeRequestRepository;
import com.barterbay.barterbay.repository.ProductRepository;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Single-Responsibility Principle (SRP)
 *   All exchange business logic lives here and NOWHERE ELSE.
 *   The controller becomes a thin HTTP adapter; it only delegates.
 *
 * SOLID – Open/Closed Principle (OCP)
 *   New lifecycle actions (e.g. COUNTER_OFFER) can be added without modifying
 *   existing methods.
 *
 * GRASP – Information Expert
 *   This class owns all knowledge about price validation rules and negotiation
 *   count limits, so it alone performs those checks.
 *
 * GRASP – Low Coupling
 *   ExchangeController no longer holds references to two repositories.
 *   It only knows about ExchangeService.
 *
 * GRASP – Creator
 *   ExchangeServiceImpl assembles the ExchangeRequest and sets its initial
 *   status – it is the natural creator of that aggregate.
 *
 * WHERE THIS FILE GOES:
 *   barterbay-backend/src/main/java/com/barterbay/barterbay/service/ExchangeServiceImpl.java
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
public class ExchangeServiceImpl implements ExchangeService {

    private static final int MAX_NEGOTIATIONS = 2;

    // ── GRASP: Low Coupling – only needed collaborators are injected ──────────
    private final ExchangeRequestRepository exchangeRepo;
    private final ProductRepository productRepo;

    @Autowired
    public ExchangeServiceImpl(ExchangeRequestRepository exchangeRepo,
                                ProductRepository productRepo) {
        this.exchangeRepo = exchangeRepo;
        this.productRepo  = productRepo;
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * GRASP – Information Expert: all validation rules live here.
     * SOLID – SRP: the controller simply calls create() and returns the result.
     */
    @Override
    public ExchangeRequest create(ExchangeRequest request) {
        validateParticipants(request);
        validateProductIds(request);

        Product requested = requireProduct(request.getRequestedProductId());
        List<Product> offered = requireAllProducts(request.getOfferedProductIds());

        validateOfferValueAgainst(requested.getPrice(), offered);

        if (request.getStatus() == null) {
            request.setStatus("PENDING");   // GRASP Creator: sets initial state
        }

        return exchangeRepo.save(request);
    }

    // =========================================================================
    // READS
    // =========================================================================

    @Override
    public ExchangeRequest findById(String id) {
        return exchangeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Exchange not found: " + id));
    }

    @Override
    public List<ExchangeRequest> findAll() {
        return exchangeRepo.findAll();
    }

    @Override
    public ExchangeRequest updateStatus(String id, String status) {
        ExchangeRequest existing = findById(id);
        existing.setStatus(status);
        return exchangeRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        if (!exchangeRepo.existsById(id)) {
            throw new NotFoundException("Exchange not found: " + id);
        }
        exchangeRepo.deleteById(id);
    }

    @Override
    public List<ExchangeRequest> findByReceiver(String receiverId) {
        return exchangeRepo.findByReceiverId(receiverId);
    }

    @Override
    public List<ExchangeRequest> findPendingByReceiver(String receiverId) {
        return exchangeRepo.findByReceiverIdAndStatus(receiverId, "PENDING");
    }

    @Override
    public List<ExchangeRequest> findNegotiatingByReceiver(String receiverId) {
        return exchangeRepo.findByReceiverIdAndStatus(receiverId, "NEGOTIATING");
    }

    @Override
    public List<ExchangeRequest> findByRequester(String requesterId) {
        return exchangeRepo.findByRequesterId(requesterId);
    }

    @Override
    public List<ExchangeRequest> findCompletedByRequester(String requesterId) {
        List<ExchangeRequest> accepted = exchangeRepo.findByRequesterIdAndStatus(requesterId, "ACCEPTED");
        List<ExchangeRequest> rejected = exchangeRepo.findByRequesterIdAndStatus(requesterId, "REJECTED");
        accepted.addAll(rejected);
        return accepted;
    }

    @Override
    public List<ExchangeRequest> findNegotiatingByRequester(String requesterId) {
        return exchangeRepo.findByRequesterIdAndStatus(requesterId, "NEGOTIATING");
    }

    // =========================================================================
    // LIFECYCLE ACTIONS
    // =========================================================================

    /**
     * SOLID – SRP: product deletion on accept is business logic → belongs here.
     * GRASP – Information Expert: only ExchangeServiceImpl knows the cleanup rule.
     */
    @Override
    public ExchangeRequest accept(String id) {
        ExchangeRequest exchange = findById(id);
        exchange.setStatus("ACCEPTED");
        ExchangeRequest saved = exchangeRepo.save(exchange);

        productRepo.deleteById(exchange.getRequestedProductId());
        exchange.getOfferedProductIds().forEach(productRepo::deleteById);

        return saved;
    }

    @Override
    public ExchangeRequest reject(String id) {
        return transition(id, ex -> ex.setStatus("REJECTED"));
    }

    /**
     * GRASP – Information Expert: negotiation-limit rule lives here, not in the controller.
     */
    @Override
    public ExchangeRequest negotiate(String id) {
        return transition(id, ex -> {
            guardNegotiationLimit(ex);
            ex.setStatus("NEGOTIATING");
            ex.setNegotiationCount(ex.getNegotiationCount() + 1);
        });
    }

    @Override
    public ExchangeRequest renegotiate(String id, List<String> newOfferedProductIds) {
        return transition(id, ex -> {
            guardNegotiationLimit(ex);
            validateOfferValueById(ex.getRequestedProductId(), newOfferedProductIds);
            ex.setOfferedProductIds(newOfferedProductIds);
            ex.setStatus("PENDING");
            ex.setNegotiationCount(ex.getNegotiationCount() + 1);
        });
    }

    @Override
    public ExchangeRequest updateOffer(String id, List<String> newOfferedProductIds) {
        return transition(id, ex -> {
            validateOfferValueById(ex.getRequestedProductId(), newOfferedProductIds);
            ex.setOfferedProductIds(newOfferedProductIds);
            // status and count unchanged – intentional
        });
    }

    // =========================================================================
    // PRIVATE HELPERS  (GRASP: Low Coupling, DRY)
    // =========================================================================

    /**
     * SOLID – OCP / DRY: single entry-point for any mutation that saves.
     * All lifecycle methods use this – avoids repeated findById + save boilerplate.
     */
    private ExchangeRequest transition(String id, java.util.function.Consumer<ExchangeRequest> mutator) {
        ExchangeRequest exchange = findById(id);
        mutator.accept(exchange);
        return exchangeRepo.save(exchange);
    }

    private void validateParticipants(ExchangeRequest request) {
        if (request.getRequesterId() == null || request.getReceiverId() == null) {
            throw new BadRequestException("Requester ID and Receiver ID are required");
        }
    }

    private void validateProductIds(ExchangeRequest request) {
        if (request.getRequestedProductId() == null
                || request.getOfferedProductIds() == null
                || request.getOfferedProductIds().isEmpty()) {
            throw new BadRequestException("Product IDs are required");
        }
    }

    private Product requireProduct(String productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found: " + productId));
    }

    private List<Product> requireAllProducts(List<String> ids) {
        List<Product> products = productRepo.findAllById(ids);
        if (products.size() != ids.size()) {
            throw new BadRequestException("One or more offered products not found");
        }
        return products;
    }

    /**
     * GRASP – Information Expert: price-validation rule centralised here.
     */
    private void validateOfferValueAgainst(double requiredValue, List<Product> offered) {
        double total = offered.stream().mapToDouble(Product::getPrice).sum();
        if (total < requiredValue) {
            throw new BadRequestException(
                "Total offered value (₹" + total + ") must be ≥ requested value (₹" + requiredValue + ")");
        }
    }

    private void validateOfferValueById(String requestedProductId, List<String> offeredIds) {
        Product requested = requireProduct(requestedProductId);
        List<Product> offered = productRepo.findAllById(offeredIds);
        validateOfferValueAgainst(requested.getPrice(), offered);
    }

    private void guardNegotiationLimit(ExchangeRequest ex) {
        if (ex.getNegotiationCount() >= MAX_NEGOTIATIONS) {
            throw new BadRequestException(
                "Maximum negotiations (" + MAX_NEGOTIATIONS + ") reached");
        }
    }
}