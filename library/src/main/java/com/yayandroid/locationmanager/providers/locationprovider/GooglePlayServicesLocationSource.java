package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.yayandroid.locationmanager.constants.RequestCode;

class GooglePlayServicesLocationSource extends LocationCallback {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final LocationRequest locationRequest;
    private final SourceListener sourceListener;

    interface SourceListener extends OnSuccessListener<LocationSettingsResponse>, OnFailureListener {
        void onConnected();

        void onSuccess(LocationSettingsResponse locationSettingsResponse);

        void onFailure(@NonNull Exception exception);

        void onLocationChanged(@NonNull Location location);
    }

    GooglePlayServicesLocationSource(Context context, LocationRequest locationRequest, SourceListener sourceListener) {
        this.sourceListener = sourceListener;
        this.locationRequest = locationRequest;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    void checkLocationSettings() {
        LocationServices.getSettingsClient(fusedLocationProviderClient.getApplicationContext())
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .build()
                )
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (sourceListener != null) sourceListener.onSuccess(locationSettingsResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (sourceListener != null) sourceListener.onFailure(exception);
                    }
                });
    }

    void startSettingsApiResolutionForResult(@NonNull ResolvableApiException resolvable, Activity activity) throws SendIntentException {
        resolvable.startResolutionForResult(activity, RequestCode.SETTINGS_API);
    }

    @SuppressWarnings("ResourceType")
    void requestLocationUpdate() {
        // This method is suited for the foreground use cases
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.myLooper());
    }

    @NonNull
    Task<Void> removeLocationUpdates() {
        return fusedLocationProviderClient.removeLocationUpdates(this);
    }

    @SuppressWarnings("ResourceType")
    @NonNull
    Task<Location> getLastLocation() {
        return fusedLocationProviderClient.getLastLocation();
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            if (sourceListener != null) sourceListener.onLocationChanged(location);
        }
    }

}
