package com.example.wangm9557.mymapsapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Network;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchWord;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private String mapView = "road";
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 5 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private Location myLocation;
    private LatLng userLocation;
    private static final int NY_LOC_ZOOM_FACTOR = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchWord = (EditText) findViewById(R.id.editText_searchWord);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLng norman = new LatLng(35, -97);
        mMap.addMarker(new MarkerOptions().position(norman).title("Born Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(norman));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }


    }

    public void changeView(View v) {
        if (mapView.equals("road")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mapView = "satelite";
        } else if (mapView.equals("satelite")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapView = "road";
        }

    }

    public void searchLocation(View v) {

        String search = searchWord.getText().toString();
        double latitude = 0;
        double longitude = 0;
        LatLng latlng = new LatLng(latitude, longitude);

        Geocoder geocoder = new Geocoder(getApplicationContext());

        try {
            List<android.location.Address> locations = geocoder.getFromLocationName(search, 5);

            for (int i = 0; i < locations.size(); i++) {
                LatLng address = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
                mMap.addMarker(new MarkerOptions().position(address).title("Search Result " + i));
            }

        } catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    public void getLocation(View v) {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //Get GPS Status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                Log.d("MyMaps", "getLocation: GPS is enabled");
            }

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: NETWORK is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                this.canGetLocation = true;
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - reuqesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);
                    Log.d("MyMaps", "getLocation: GPS");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }
                else if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("MyMaps", "Permissions granted");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);


                        Log.d("MyMaps", "getLocation: GPS");
                        Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                    }

                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "Exception in getLocation");
            e.printStackTrace();
        }
    }

    android.location.LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output in Log.d and Toast that GPS is enabled and working

            //Drop a marker on map- create a method called dropAmarker


            Log.d("MyMaps", "locationListenerGPS: onLocationChanged utilized and working");
            Toast.makeText(getApplicationContext(), "LocationListenerGPS onLocationChanged working", Toast.LENGTH_SHORT).show();
            double lat1 = location.getLatitude();
            double long1 = location.getLongitude();
            LatLng latlng = new LatLng(lat1, long1);
            addAmarker(LocationManager.NETWORK_PROVIDER);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationManager.removeUpdates(locationListenerGPS);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output in Log.d and Toast that GPS is enabled and working
            //Set up a switch statement to check the status of the input parameter
            //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
            //case LocationProvider.OUT_OF_SERVICE -->  request updates from NETWORK_PROVIDER
            //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            //case default --> request updates from NETWORK_PROVIDER

            //Log.d and Toast

            Log.d("MyMaps", "locationListenerGPS: onStatusChanged utilized and working");
            Toast.makeText(getApplicationContext(), "LocationListenerGPS onStatusChanged", Toast.LENGTH_SHORT).show();

            //Switch/case statement
            switch(status){
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "LocationProvider is available");
                    Toast.makeText(getApplicationContext(), "LocationProvider is available", Toast.LENGTH_SHORT).show();
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "LocationProvider out of service");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);


                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "LocationProvider is temporarily unavailable");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);


                default:
                    Log.d("MyMaps", "LocationProvider default");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
            }


        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    android.location.LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output in Log.d and Toast that GPS is enabled and working

            //Drop a marker on map- create a method called dropAmarker

            // Relaunch the network provider requestLocationUpdates(NETWORK_PROVIDER)

            //Log.d and Toast
            Log.d("MyMaps", "locationListenerNETWORK: onLocationChanged utilized and working");
            Toast.makeText(getApplicationContext(), "LocationListenerNETWORK onStatusChanged", Toast.LENGTH_SHORT).show();


            double long2 = location.getLongitude();
            addAmarker(LocationManager.NETWORK_PROVIDER);


            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        locationListenerNetwork);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in Log.d and Toast
            Log.d("MyMaps", "locationListenerNETWORK: onStatusChanged utilized and working");
            Toast.makeText(getApplicationContext(), "LocationListenerNETWORK onStatusChanged", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    public void addAmarker(String provider) {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);

        }

        if(myLocation == null){
            //Display a message via Log.d and/or Toast
            Log.d("myMaps", "in draw Marker: myLocation is Null");
        } else{
            //get the user Location
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            Log.d("myMaps", "in draw Marker at: " + userLocation.latitude + " " + userLocation.longitude);

            CameraUpdate update = CameraUpdateFactory.newLatLng(userLocation, NY_LOC_ZOOM_FACTOR);

            //Drop the actual marker on the map
            //If using circles, reference Android Circle Class
            Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.RED).fillColor(Color.RED));

            mMap.animateCamera(update);
        }
    }
}
