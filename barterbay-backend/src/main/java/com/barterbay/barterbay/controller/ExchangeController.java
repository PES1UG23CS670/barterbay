package com.barterbay.barterbay.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barterbay.barterbay.exception.BadRequestException;
import com.barterbay.barterbay.model.ExchangeRequest;
import com.barterbay.barterbay.model.Product;
import com.barterbay.barterbay.repository.ExchangeRequestRepository;
import com.barterbay.barterbay.repository.ProductRepository;

@RestController
@RequestMapping("/api/exchanges")
@CrossOrigin
public class ExchangeController {

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ExchangeRequest> createExchangeRequest(@RequestBody ExchangeRequest request) {
        
        try {
            System.out.println("DEBUG - Received exchange request:");
            System.out.println("  Requester ID: " + request.getRequesterId());
            System.out.println("  Receiver ID: " + request.getReceiverId());
            System.out.println("  Requested Product ID: " + request.getRequestedProductId());
            System.out.println("  Offered Product IDs: " + request.getOfferedProductIds());
            
            if (request.getRequesterId() == null || request.getReceiverId() == null) {
                throw new BadRequestException("Requester ID and Receiver ID are required");
            }

            if (request.getRequestedProductId() == null || request.getOfferedProductIds() == null || request.getOfferedProductIds().isEmpty()) {
                throw new BadRequestException("Product IDs are required");
            }

            // Fetch requested product and validate it exists
            Optional<Product> requestedProductOpt = productRepository.findById(request.getRequestedProductId());
            if (!requestedProductOpt.isPresent()) {
                throw new BadRequestException("Requested product not found");
            }
            Product requestedProduct = requestedProductOpt.get();
            double requestedProductPrice = requestedProduct.getPrice();

            // Fetch all offered products and validate they exist
            List<Product> offeredProducts = productRepository.findAllById(request.getOfferedProductIds());
            if (offeredProducts.size() != request.getOfferedProductIds().size()) {
                throw new BadRequestException("One or more offered products not found");
            }

            // Calculate total price of offered products
            double totalOfferedPrice = 0;
            for (Product product : offeredProducts) {
                totalOfferedPrice += product.getPrice();
                System.out.println("  Offered Product: " + product.getId() + " (" + product.getItemName() + ") - Price: ₹" + product.getPrice());
            }

            System.out.println("  Requested Product Price: ₹" + requestedProductPrice);
            System.out.println("  Total Offered Price: ₹" + totalOfferedPrice);

            // Validate that total offered price >= requested product price
            if (totalOfferedPrice < requestedProductPrice) {
                throw new BadRequestException("Total value of offered products (₹" + totalOfferedPrice + 
                    ") must be greater than or equal to requested product value (₹" + requestedProductPrice + ")");
            }

            // Set initial status
            if (request.getStatus() == null) {
                request.setStatus("PENDING");
            }

            ExchangeRequest savedRequest = exchangeRequestRepository.save(request);
            System.out.println("DEBUG - Exchange request saved successfully with ID: " + savedRequest.getId());
            
            return ResponseEntity.status(201).body(savedRequest);
        } catch (BadRequestException e) {
            System.out.println("DEBUG - BadRequestException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("DEBUG - Exception occurred: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create exchange request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRequest> getExchangeRequest(@PathVariable String id) {
        return exchangeRequestRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRequest>> getAllExchangeRequests() {
        List<ExchangeRequest> requests = exchangeRequestRepository.findAll();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRequest> updateExchangeRequest(
            @PathVariable String id,
            @RequestBody ExchangeRequest request) {
        
        return exchangeRequestRepository.findById(id)
            .map(existing -> {
                if (request.getStatus() != null) {
                    existing.setStatus(request.getStatus());
                }
                return ResponseEntity.ok(exchangeRequestRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeRequest(@PathVariable String id) {
        if (exchangeRequestRepository.existsById(id)) {
            exchangeRequestRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<ExchangeRequest>> getExchangesForReceiver(@PathVariable String receiverId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByReceiverId(receiverId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/receiver/{receiverId}/pending")
    public ResponseEntity<List<ExchangeRequest>> getPendingExchangesForReceiver(@PathVariable String receiverId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByReceiverIdAndStatus(receiverId, "PENDING");
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ExchangeRequest> acceptExchange(@PathVariable String id) {
        try {
            return exchangeRequestRepository.findById(id)
                .map(existing -> {
                    existing.setStatus("ACCEPTED");
                    ExchangeRequest updated = exchangeRequestRepository.save(existing);
                    
                    // Delete the requested product from the database
                    productRepository.deleteById(existing.getRequestedProductId());
                    System.out.println("DEBUG - Deleted requested product: " + existing.getRequestedProductId());
                    
                    // Delete all offered products from the database
                    for (String offeredProductId : existing.getOfferedProductIds()) {
                        productRepository.deleteById(offeredProductId);
                        System.out.println("DEBUG - Deleted offered product: " + offeredProductId);
                    }
                    
                    System.out.println("DEBUG - Exchange request " + id + " ACCEPTED successfully and all involved products deleted");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in acceptExchange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to accept exchange: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ExchangeRequest> rejectExchange(@PathVariable String id) {
        try {
            return exchangeRequestRepository.findById(id)
                .map(existing -> {
                    existing.setStatus("REJECTED");
                    ExchangeRequest updated = exchangeRequestRepository.save(existing);
                    System.out.println("DEBUG - Exchange request " + id + " REJECTED successfully");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in rejectExchange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to reject exchange: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/negotiate")
    public ResponseEntity<ExchangeRequest> negotiateExchange(@PathVariable String id) {
        try {
            return exchangeRequestRepository.findById(id)
                .map(existing -> {
                    // Check if max negotiations (2) have been reached
                    if (existing.getNegotiationCount() >= 2) {
                        throw new RuntimeException("Maximum negotiations (2) reached. Receiver can only Accept or Reject now.");
                    }
                    
                    existing.setStatus("NEGOTIATING");
                    existing.setNegotiationCount(existing.getNegotiationCount() + 1);
                    ExchangeRequest updated = exchangeRequestRepository.save(existing);
                    System.out.println("DEBUG - Exchange request " + id + " NEGOTIATING (Count: " + updated.getNegotiationCount() + ")");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in negotiateExchange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to negotiate exchange: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/renegotiate")
    public ResponseEntity<ExchangeRequest> renegotiateExchange(@PathVariable String id, @RequestBody ExchangeRequest request) {
        try {
            return exchangeRequestRepository.findById(id)
                .map(existing -> {
                    // Check if max negotiations (2) have been reached before allowing update
                    if (existing.getNegotiationCount() >= 2) {
                        throw new RuntimeException("Maximum negotiations (2) reached. No more modifications allowed.");
                    }
                    
                    // Validate that offered products value >= requested product value
                    validateOfferValue(existing.getRequestedProductId(), request.getOfferedProductIds());
                    
                    // Update offered products
                    existing.setOfferedProductIds(request.getOfferedProductIds());
                    existing.setStatus("PENDING");
                    existing.setNegotiationCount(existing.getNegotiationCount() + 1);
                    
                    ExchangeRequest updated = exchangeRequestRepository.save(existing);
                    System.out.println("DEBUG - Exchange request " + id + " RENEGOTIATED (Count: " + updated.getNegotiationCount() + ")");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in renegotiateExchange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to renegotiate exchange: " + e.getMessage());
        }
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ExchangeRequest>> getExchangesForRequester(@PathVariable String requesterId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByRequesterId(requesterId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requester/{requesterId}/completed")
    public ResponseEntity<List<ExchangeRequest>> getCompletedExchangesForRequester(@PathVariable String requesterId) {
        // Get exchanges with status ACCEPTED or REJECTED
        List<ExchangeRequest> accepted = exchangeRequestRepository.findByRequesterIdAndStatus(requesterId, "ACCEPTED");
        List<ExchangeRequest> rejected = exchangeRequestRepository.findByRequesterIdAndStatus(requesterId, "REJECTED");
        accepted.addAll(rejected);
        return ResponseEntity.ok(accepted);
    }

    @GetMapping("/receiver/{receiverId}/negotiating")
    public ResponseEntity<List<ExchangeRequest>> getNegotiatingExchangesForReceiver(@PathVariable String receiverId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByReceiverIdAndStatus(receiverId, "NEGOTIATING");
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requester/{requesterId}/negotiating")
    public ResponseEntity<List<ExchangeRequest>> getNegotiatingExchangesForRequester(@PathVariable String requesterId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByRequesterIdAndStatus(requesterId, "NEGOTIATING");
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/update-offer")
    public ResponseEntity<ExchangeRequest> updateOfferProducts(@PathVariable String id, @RequestBody ExchangeRequest request) {
        try {
            return exchangeRequestRepository.findById(id)
                .map(existing -> {
                    // Validate that offered products value >= requested product value
                    validateOfferValue(existing.getRequestedProductId(), request.getOfferedProductIds());
                    
                    // Update offered products without changing status or incrementing negotiation count
                    existing.setOfferedProductIds(request.getOfferedProductIds());
                    
                    ExchangeRequest updated = exchangeRequestRepository.save(existing);
                    System.out.println("DEBUG - Exchange request " + id + " offer updated (status remains: " + updated.getStatus() + ")");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in updateOfferProducts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update offer: " + e.getMessage());
        }
    }

    // Helper method to validate that offered products value >= requested product value
    private void validateOfferValue(String requestedProductId, List<String> offeredProductIds) {
        Optional<Product> requestedProductOpt = productRepository.findById(requestedProductId);
        if (!requestedProductOpt.isPresent()) {
            throw new RuntimeException("Requested product not found");
        }

        double requestedProductValue = requestedProductOpt.get().getPrice();
        double totalOfferedValue = 0;

        for (String offeredProductId : offeredProductIds) {
            Optional<Product> offeredProductOpt = productRepository.findById(offeredProductId);
            if (offeredProductOpt.isPresent()) {
                totalOfferedValue += offeredProductOpt.get().getPrice();
            }
        }

        if (totalOfferedValue < requestedProductValue) {
            throw new RuntimeException("The total value of offered products (₹" + totalOfferedValue + 
                ") must be at least equal to the requested product value (₹" + requestedProductValue + ")");
        }
    }
}


