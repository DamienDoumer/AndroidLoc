package com.example.dtohi.saferoad.Listeners;

import android.location.Location;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by dtohi on 9/6/2017.
 */

public interface ILocationInteropListener {
    void startLocationUpdates(LocationRequest locationRequest);
    void setUserCurrentLocation(Location location);
}
