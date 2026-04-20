package com.barterbay.frontend.filter;

import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * INTERFACE: Filter<T>
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * SOLID – Open/Closed Principle (OCP)
 *   - Closed for modification: stable filter contract
 *   - Open for extension: implement new filters without changing this interface
 *
 * PURPOSE:
 *   Defines the contract for all filter implementations. Allows multiple
 *   filters to be applied to product lists independently.
 *
 * USAGE:
 *   new ByCategoryFilter("Electronics").apply(products);
 *   new ByPriceFilter(100, 5000).apply(products);
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface Filter<T> {
    
    List<T> apply(List<T> items);
    
    String getFilterName();
}
