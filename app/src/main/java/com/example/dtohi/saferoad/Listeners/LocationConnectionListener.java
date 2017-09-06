package com.example.dtohi.saferoad.Listeners;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by dtohi on 9/5/2017.
 */

//I use the Bill Pugh Singleton to avoid JVM errors eith other Singleton patterns
public class LocationConnectionListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Fragment mapFragment;
    private final int TIME_INTERVAL = 10000;
    private final int FASTEST_TIME_INTERVAL = 5000;
    LocationRequest mLocationRequest;
    private final  int REQUEST_CHECK_SETTINGS = 1;
    private ILocationInteropListener locationInteropListener;

    public LocationConnectionListener(Fragment fragment, ILocationInteropListener locationInteropListener)
    {
        this.locationInteropListener = locationInteropListener;
        mapFragment = fragment;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(TIME_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_TIME_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void checkLocationSettings()
    {
        //get teh users location settings.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(mapFragment.getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(mapFragment.getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(mapFragment.getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
        task.addOnSuccessListener(mapFragment.getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });
    }

    private LocationConnectionListener()
    {}


    private static class SingletonHelper
    {
        private static final LocationConnectionListener INSTANCE = new LocationConnectionListener();
    }

    public static LocationConnectionListener getInstance(){
        return SingletonHelper.INSTANCE;
    }

    //Starts listenning to location changes
    protected void startLocationUpdates(){
        locationInteropListener.startLocationUpdates(mLocationRequest);
    }

    @Override
    public void onLocationChanged(Location location) {

        locationInteropListener.setUserCurrentLocation(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
;
    }
}
