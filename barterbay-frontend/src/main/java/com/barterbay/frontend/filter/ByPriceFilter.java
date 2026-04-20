package com.barterbay.frontend.filter;

import com.barterbay.frontend.model.Product;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * CONCRETE FILTER: ByPriceFilter
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * SOLID – Open/Closed Principle
 *   - Extends AbstractFilter without modifying existing code
 *   - Implements range-based filtering independent of category filtering
 *
 * PURPOSE:
 *   Filters products within a price range [minPrice, maxPrice] (inclusive).
 *
 * USAGE:
 *   Filter<Product> priceFilter = new ByPriceFilter(100, 5000);
 *   List<Product> affordable = priceFilter.apply(allProducts);
 *
 * DESIGN:
 *   Composable with other filters for complex multi-criteria filtering.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ByPriceFilter extends AbstractFilter<Product> {
    
    private final double minPrice;
    private final double maxPrice;
    
    /**
     * Creates a filter for products within a price range.
     *
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     */
    public ByPriceFilter(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    /**
     * Tests whether a product's price falls within the specified range.
     *
     * @param product the product to test
     * @return true if price is >= minPrice and <= maxPrice, false otherwise
     */
    @Override
    protected boolean test(Product product) {
        if (product == null) {
            return false;
        }
        
        double price = product.getPrice();
        return price >= minPrice && price <= maxPrice;
    }
    
    @Override
    public String getFilterName() {
        return "Price Filter: ₹" + minPrice + " - ₹" + maxPrice;
    }
}
