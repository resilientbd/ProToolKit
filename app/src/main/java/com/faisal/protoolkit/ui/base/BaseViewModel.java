package com.faisal.protoolkit.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Base ViewModel providing loading/error observables.
 */
public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    protected void setLoading(boolean loading) {
        isLoading.postValue(loading);
    }

    protected void postError(String message) {
        errorMessage.postValue(message);
    }
}
