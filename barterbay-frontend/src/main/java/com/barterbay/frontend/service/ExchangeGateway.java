package com.barterbay.frontend.service;

import java.io.IOException;
import java.util.List;

import com.barterbay.frontend.model.ExchangeRequest;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SOLID – Interface-Segregation Principle (ISP)
 *   Only exchange-domain operations.  Product queries remain in ProductService.
 *
 * SOLID – Dependency-Inversion Principle (DIP)
 *   Frontend controllers depend on this interface; the concrete HTTP
 *   implementation (ExchangeGatewayImpl) can be swapped for a stub in tests.
 *
 * GRASP – Low Coupling
 *   Controllers import this interface; they are decoupled from HTTP/Gson details.
 *
 * WHERE THIS FILE GOES:
 *   barterbay-frontend/src/main/java/com/barterbay/frontend/service/ExchangeGateway.java
 *   (NEW file – add to the project)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface ExchangeGateway {

    void submit(String requesterId, String receiverId,
                String requestedProductId, List<String> offeredProductIds) throws IOException;

    List<ExchangeRequest> getPendingForReceiver(String userId)         throws IOException;
    List<ExchangeRequest> getNegotiatingForReceiver(String userId)     throws IOException;
    List<ExchangeRequest> getNegotiatingForRequester(String userId)    throws IOException;
    List<ExchangeRequest> getCompletedForRequester(String userId)      throws IOException;

    void accept(String exchangeId)      throws IOException;
    void reject(String exchangeId)      throws IOException;
    void negotiate(String exchangeId)   throws IOException;

    void renegotiate(String exchangeId, List<String> offeredProductIds)  throws IOException;
    void updateOffer(String exchangeId, List<String> offeredProductIds)  throws IOException;
}