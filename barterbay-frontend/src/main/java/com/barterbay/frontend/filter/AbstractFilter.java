package com.barterbay.frontend.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ABSTRACT CLASS: AbstractFilter<T>
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * SOLID – Template Method Pattern + OCP
 *   - Defines the overall filter algorithm in apply()
 *   - Concrete filters override the test condition
 *   - No need to re-implement filtering logic in child classes
 *
 * PURPOSE:
 *   Provides the base implementation for all filters. Child classes only
 *   need to implement the test condition.
 *
 * ARCHITECTURE:
 *   AbstractFilter<T> (this class)
 *       ↓
 *   ByCategoryFilter extends AbstractFilter<Product>
 *   ByPriceFilter extends AbstractFilter<Product>
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public abstract class AbstractFilter<T> implements Filter<T> {
    
    /**
     * The filtering logic applies Stream API to check each item against
     * the test condition provided by subclasses.
     *
     * @param items the list to filter
     * @return new filtered list
     */
    @Override
    public List<T> apply(List<T> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        return items.stream()
                    .filter(this::test)
                    .collect(Collectors.toList());
    }
    
    /**
     * Template method for subclasses to override.
     * Determines whether an item passes the filter.
     *
     * @param item the item to test
     * @return true if item passes the filter, false otherwise
     */
    protected abstract boolean test(T item);
}
