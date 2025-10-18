package com.faisal.protoolkit.data.billing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.faisal.protoolkit.billing.BillingManager;

import java.util.List;

/**
 * Repository for managing billing operations.
 */
public class BillingRepository implements BillingManager.BillingUpdatesListener {

    private final BillingManager billingManager;
    private final MutableLiveData<Boolean> isInitialized = new MutableLiveData<>(false);
    private final MutableLiveData<List<Purchase>> purchases = new MutableLiveData<>();
    private final MutableLiveData<List<ProductDetails>> products = new MutableLiveData<>();

    public BillingRepository(@NonNull Application application) {
        billingManager = new BillingManager(application);
        billingManager.setBillingUpdatesListener(this);
    }

    public void startBillingConnection() {
        billingManager.startConnection();
    }

    public void endBillingConnection() {
        billingManager.endConnection();
    }

    public void purchaseProduct(String productId) {
        // This will be called from the activity
    }

    public boolean isPremiumPurchased() {
        return billingManager.isPremiumPurchased();
    }

    public boolean isRemoveAdsPurchased() {
        return billingManager.isRemoveAdsPurchased();
    }

    public boolean isUnlockAllPurchased() {
        return billingManager.isUnlockAllPurchased();
    }

    public ProductDetails getProductDetails(String productId) {
        return billingManager.getProductDetails(productId);
    }

    // LiveData getters
    public LiveData<Boolean> isInitialized() {
        return isInitialized;
    }

    public LiveData<List<Purchase>> getPurchases() {
        return purchases;
    }

    public LiveData<List<ProductDetails>> getProducts() {
        return products;
    }

    // BillingUpdatesListener implementation
    @Override
    public void onBillingClientSetupFinished() {
        isInitialized.postValue(true);
    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchases) {
        this.purchases.postValue(purchases);
    }

    @Override
    public void onProductsUpdated(List<ProductDetails> products) {
        this.products.postValue(products);
    }
}