package com.faisal.protoolkit.billing;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages in-app purchases for the application.
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";
    
    // Product IDs
    public static final String PRODUCT_PREMIUM = "protoolkit_premium";
    public static final String PRODUCT_REMOVE_ADS = "protoolkit_remove_ads";
    public static final String PRODUCT_UNLOCK_ALL = "protoolkit_unlock_all";
    
    private final Application application;
    private BillingClient billingClient;
    private final List<Purchase> purchases = new ArrayList<>();
    private final ConcurrentHashMap<String, ProductDetails> productDetailsMap = new ConcurrentHashMap<>();
    private boolean isServiceConnected = false;
    private final List<Runnable> pendingPurchases = new ArrayList<>();
    
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();
        void onPurchasesUpdated(List<Purchase> purchases);
        void onProductsUpdated(List<ProductDetails> products);
    }
    
    private BillingUpdatesListener updatesListener;

    public BillingManager(@NonNull Application application) {
        this.application = application;
        initializeBillingClient();
    }
    
    private void initializeBillingClient() {
        billingClient = BillingClient.newBuilder(application)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }
    
    public void setBillingUpdatesListener(BillingUpdatesListener listener) {
        updatesListener = listener;
    }
    
    public void startConnection() {
        if (!isServiceConnected) {
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing client connected");
                        isServiceConnected = true;
                        
                        // Query existing purchases
                        queryPurchases();
                        
                        // Query product details
                        queryProductDetails();
                        
                        // Process any pending purchases
                        processPendingPurchases();
                        
                        if (updatesListener != null) {
                            updatesListener.onBillingClientSetupFinished();
                        }
                    } else {
                        Log.e(TAG, "Billing client setup failed: " + billingResult.getDebugMessage());
                    }
                }
                
                @Override
                public void onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing client disconnected");
                    isServiceConnected = false;
                }
            });
        }
    }
    
    public void endConnection() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
        isServiceConnected = false;
    }
    
    private void queryPurchases() {
        if (!isServiceConnected) {
            Log.w(TAG, "Billing client not connected, cannot query purchases");
            return;
        }
        
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchasesList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        purchases.clear();
                        purchases.addAll(purchasesList);
                        
                        // Acknowledge purchases if needed
                        for (Purchase purchase : purchasesList) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                if (!purchase.isAcknowledged()) {
                                    acknowledgePurchase(purchase);
                                }
                            }
                        }
                        
                        if (updatesListener != null) {
                            updatesListener.onPurchasesUpdated(purchases);
                        }
                    } else {
                        Log.e(TAG, "Query purchases failed: " + billingResult.getDebugMessage());
                    }
                }
        );
    }
    
    private void queryProductDetails() {
        if (!isServiceConnected) {
            Log.w(TAG, "Billing client not connected, cannot query product details");
            return;
        }
        
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PREMIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_UNLOCK_ALL)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        
        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, 
                                                @NonNull List<ProductDetails> productDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    productDetailsMap.clear();
                    for (ProductDetails productDetails : productDetailsList) {
                        productDetailsMap.put(productDetails.getProductId(), productDetails);
                    }
                    
                    if (updatesListener != null) {
                        updatesListener.onProductsUpdated(productDetailsList);
                    }
                } else {
                    Log.e(TAG, "Query product details failed: " + billingResult.getDebugMessage());
                }
            }
        });
    }
    
    public void launchBillingFlow(Activity activity, String productId) {
        if (!isServiceConnected) {
            Log.w(TAG, "Billing client not connected, adding purchase to pending queue");
            pendingPurchases.add(() -> launchBillingFlow(activity, productId));
            startConnection();
            return;
        }
        
        ProductDetails productDetails = productDetailsMap.get(productId);
        if (productDetails == null) {
            Log.e(TAG, "Product details not found for: " + productId);
            return;
        }
        
        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        productDetailsParamsList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
        );
        
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Launch billing flow failed: " + billingResult.getDebugMessage());
        }
    }
    
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, 
                                  @Nullable List<Purchase> purchasesList) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesList != null) {
            for (Purchase purchase : purchasesList) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled the purchase flow");
        } else {
            Log.e(TAG, "Purchase failed with code: " + billingResult.getResponseCode());
        }
    }
    
    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Add to our list
            purchases.add(purchase);
            
            // Acknowledge the purchase if needed
            if (!purchase.isAcknowledged()) {
                acknowledgePurchase(purchase);
            }
            
            // Notify listeners
            if (updatesListener != null) {
                updatesListener.onPurchasesUpdated(purchases);
            }
        }
    }
    
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged: " + purchase.getOrderId());
            } else {
                Log.e(TAG, "Acknowledge purchase failed: " + billingResult.getDebugMessage());
            }
        });
    }
    
    public boolean isPremiumPurchased() {
        return isProductPurchased(PRODUCT_PREMIUM);
    }
    
    public boolean isRemoveAdsPurchased() {
        return isProductPurchased(PRODUCT_REMOVE_ADS);
    }
    
    public boolean isUnlockAllPurchased() {
        return isProductPurchased(PRODUCT_UNLOCK_ALL);
    }
    
    private boolean isProductPurchased(String productId) {
        for (Purchase purchase : purchases) {
            if (purchase.getProducts().contains(productId) && 
                purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                return true;
            }
        }
        return false;
    }
    
    public ProductDetails getProductDetails(String productId) {
        return productDetailsMap.get(productId);
    }
    
    public List<Purchase> getPurchases() {
        return new ArrayList<>(purchases);
    }
    
    private void processPendingPurchases() {
        for (Runnable runnable : pendingPurchases) {
            runnable.run();
        }
        pendingPurchases.clear();
    }
}