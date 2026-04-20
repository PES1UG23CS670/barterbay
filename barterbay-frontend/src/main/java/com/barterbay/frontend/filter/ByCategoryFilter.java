package com.barterbay.frontend.filter;

import com.barterbay.frontend.model.Product;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * CONCRETE FILTER: ByCategoryFilter
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * SOLID – Open/Closed Principle
 *   - Extends AbstractFilter without modifying existing code
 *   - New filter for different criteria just create another child class
 *
 * PURPOSE:
 *   Filters products by a specific category. Case-insensitive and null-safe.
 *
 * USAGE:
 *   Filter<Product> categoryFilter = new ByCategoryFilter("Electronics");
 *   List<Product> electronics = categoryFilter.apply(allProducts);
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ByCategoryFilter extends AbstractFilter<Product> {
    
    private final String category;
    
    /**
     * Creates a filter for the given category.
     *
     * @param category the category to filter by (case-insensitive)
     */
    public ByCategoryFilter(String category) {
        this.category = category;
    }
    
    /**
     * Tests whether a product belongs to the target category.
     *
     * @param product the product to test
     * @return true if product's category matches (case-insensitive), false otherwise
     */
    @Override
    protected boolean test(Product product) {
        if (product == null || product.getCategory() == null) {
            return false;
        }
        
        return product.getCategory()
                      .equalsIgnoreCase(category);
    }
    
    @Override
    public String getFilterName() {
        return "Category Filter: " + category;
    }
}
