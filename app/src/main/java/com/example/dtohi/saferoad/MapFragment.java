package com.example.dtohi.saferoad;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dtohi.saferoad.Listeners.ILocationInteropListener;
import com.example.dtohi.saferoad.Listeners.LocationConnectionListener;
import com.example.dtohi.saferoad.Listeners.OnFragmentInteractionListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    GoogleApiClient googleApiClient;

    private OnFragmentInteractionListener mListener;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownUserLocation;
    private LatLng defaultLocation;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99, DEFAULT_ZOOM = 17;
    private static Marker userCurrentLocationMarker;
    private LocationConnectionListener locationConnectionListener;
    private MapView mapView;

    public MapFragment() {
        // Required empty public constructor

        locationConnectionListener = new LocationConnectionListener(this, new ILocationInteropListener() {
            @Override
            public void startLocationUpdates(LocationRequest locationRequest) {
                if (ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationConnectionListener);
                }
            }

            @Override
            public void setUserCurrentLocation(Location location) {
                if(userCurrentLocationMarker != null)
                {
                    userCurrentLocationMarker.remove();
                }
                userCurrentLocationMarker = setUserLocationOnMap(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        //Setup the google APIClients
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(locationConnectionListener)
                .addOnConnectionFailedListener(locationConnectionListener)
                .build();

        googleApiClient.connect();
        locationConnectionListener.checkLocationSettings();

        //THis is the Default Locaiton where the point will be pointing to on the earth
        defaultLocation = new LatLng(3.82657414, 11.50749207);
        requestLocationPermission();
        getUserCurrentLocation();

        //Gets teh mapVIew
        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

//        ((SupportMapFragment) getFragmentManager()
//                .findFragmentById(R.id.map)).getMapAsync(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Makes sure the activity calling this implemnets OnFragmentInteraction...
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        LocationServices.FusedLocationApi
                .removeLocationUpdates(googleApiClient, locationConnectionListener);
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng latLng = null;

        if(lastKnownUserLocation != null)
        {
            latLng =  new LatLng(lastKnownUserLocation.getLatitude(),
                    lastKnownUserLocation.getLongitude());
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(userCurrentLocationMarker!=null){userCurrentLocationMarker.remove();}
            userCurrentLocationMarker = setUserLocationOnMap(latLng);
        }
    }

    private Marker setUserLocationOnMap(LatLng latLng){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        return setMarker(latLng, getString(R.string.userCurrentLocation));
    }
    private Marker setMarker(LatLng latLng, String title){

        MarkerOptions unsafePlace = new MarkerOptions()
                .position(latLng)
                .title(title);

        return mMap.addMarker(unsafePlace);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                else
                {
                    //Permissions not granted
                    notiFyUser(getResources().getString(R.string.LocationNotGranted));
                }
            }
        }
    }


    ///Check if user has granted permissions and requests if needed
    public boolean requestLocationPermission() {

        //If permissions are not granted
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

            return false;
        }
        //If permissions are granted.
        else {
            return true;
        }
    }

    //Send a Toast notification to the user.
    public void notiFyUser(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void getUserCurrentLocation()
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        //If location permissions are already granted, proceed and ask user location
        //Else, request location permission.
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {

            //Requests the last know user's location.
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>()
            {
                @Override
                public void onSuccess(Location location) {
                    if(location != null)
                    {
                        lastKnownUserLocation = location;
                    }
                }
            })
            .addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ;
                }
            });
        }
        else
        {
            requestLocationPermission();
        }
    }

    private void checkIfLocationSettingsAreSatisfied()
    {
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(mLocationRequest);
    }
}
