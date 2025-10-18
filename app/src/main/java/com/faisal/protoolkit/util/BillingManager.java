package com.faisal.protoolkit.util;

/**
 * Manages in-app billing products and purchases.
 */
public class BillingManager {
    // Product IDs
    public static final String PRODUCT_PREMIUM = "premium_upgrade";
    public static final String PRODUCT_REMOVE_ADS = "remove_ads";
    public static final String PRODUCT_UNLOCK_ALL = "unlock_all_features";
    
    // Test product IDs
    public static final String TEST_PRODUCT_PURCHASED = "android.test.purchased";
    public static final String TEST_PRODUCT_CANCELED = "android.test.canceled";
    public static final String TEST_PRODUCT_UNAVAILABLE = "android.test.item_unavailable";
    
    private BillingManager() {
        // Private constructor to prevent instantiation
    }
}