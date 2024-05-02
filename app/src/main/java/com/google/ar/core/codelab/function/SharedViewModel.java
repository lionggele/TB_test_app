package com.google.ar.core.codelab.function;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String> imageUrl = new MutableLiveData<>();
    private final MutableLiveData<Double> realWorldDiameter = new MutableLiveData<>();
    private final MutableLiveData<String> interpretation = new MutableLiveData<>();

    public LiveData<String> getImageUrl() {
        return imageUrl;
    }
    private MutableLiveData<Boolean> vaccinationStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> closeContactStatus = new MutableLiveData<>();

    public LiveData<Boolean> getVaccinationStatus() {
        return vaccinationStatus;
    }
    public void setVaccinationStatus(boolean status) {
        Log.d("SharedViewModel", "Setting vaccination status: " + status);
        vaccinationStatus.setValue(status);
    }
    public LiveData<Boolean> getCloseContactStatus() {
        return closeContactStatus;
    }
    public void setCloseContactStatus(boolean status) {
        Log.d("CloseContact", "Setting risk status: " + status);
        closeContactStatus.setValue(status);
    }
    public void setImageUrl(String url) {
        imageUrl.setValue(url);
    }
    public void setRealWorldDiameter(Double value) {
        realWorldDiameter.setValue(value);
    }
    public LiveData<Double> getRealWorldDiameter() {
        return realWorldDiameter;
    }

    public void setInterpretation(String value) {
        interpretation.setValue(value);
    }

    public LiveData<String> getInterpretation() {
        return interpretation;
    }

}

