package com.example.wangm9557.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private boolean isTracked = false;
    private Location myLocation;
    private static final float MY_LOC_ZOOM_FACTOR = 20.0f;
    private boolean dotColor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //initial marker
        LatLng birth = new LatLng(35.222569, -97.439476	);
        mMap.addMarker(new MarkerOptions().position(birth).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birth));
        Log.d("My Map", "home location works");

        //current location using GPS, not LocationManager
  /*      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("My Map", "Permission failed, asking for fine permission now");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("My Map", "Permission failed, asking for coarse permission now");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 4);
        }
        mMap.setMyLocationEnabled(true);
        Log.d("My Map", "Current Location dropped"); */

        //(line below is wrong, deprecated method)
      /*  Location location = googleMap.getMyLocation();
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("My Map", "current location retrieved");
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation)); */
    }

    //changes the map type from "normal" to "satellite" view
    public void changeMapType(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get gps status
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsEnabled == true) {
                Log.d("MyMaps", "getLocation: GPS is enabled");
            }

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled == true) {
                Log.d("MyMaps", "getLocation: Network is enabled");
            }

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No Provider is enabled");
            } else {
                canGetLocation = true;
                if (isGpsEnabled == true) {
                    Log.d("MyMaps", "getLocation: GPS enabled & requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Log.d("MyMaps", "Permissions granted");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMaps", "getLocation: GPS update request is happening");
                    Toast.makeText(this, "Currently Using GPS", Toast.LENGTH_SHORT).show();
                    /*if (dotColor == true) {
                        dotColor = false;
                    } */
                }
                if (isNetworkEnabled == true) {
                    Log.d("MyMaps", "getLocation: Network enabled & requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request is happening");
                    Toast.makeText(this, "Currently Using Network", Toast.LENGTH_SHORT).show();
                    //dotColor = true;
                }

            }
        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }

    }

    public void trackMe(View view) {
        isTracked = true;
        if (isTracked == true) {
            Toast.makeText(MapsActivity.this, "Currently getting your location", Toast.LENGTH_SHORT).show();
            getLocation();
            isTracked = false;
        }
        if (isTracked == false) {
            return;
        }

    }

    public void searchPlaces(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.searchField);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Search Results"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output a message in log.D and toast
            Log.d("MyMaps", "GPS Location has changed");
            Toast.makeText(MapsActivity.this, "GPS Location has changed", Toast.LENGTH_SHORT).show();

            //drop a marker on the map (create a method called drop a marker)
            dropMarker(LocationManager.GPS_PROVIDER);
            Log.d("MyMaps", "called dropmarker() method from GPS");

            // disable network updates (see locationManager API to remove updates)
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
            dotColor = true;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //setup a switch statement on status
            //case: where location provider is available (output a Log.D or toast)
            //case: location LocationProvider.OUT_OF_SERvIce-> request updates from network provider
            //case: locationProvider.TEMPORARILY_UNAVAILABLE --> request updates from network provider

            switch (status) {
                case LocationProvider.AVAILABLE:

                    Log.d("MyMaps", "LocationProvider is available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

            }


        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output a message in log.D and toast
            Log.d("MyMaps", "Network Location has changed");
            Toast.makeText(MapsActivity.this, "Network Location has changed", Toast.LENGTH_SHORT).show();

            //drop a marker on the map (create a method called drop a marker)
            dropMarker(LocationManager.NETWORK_PROVIDER);
            Log.d("MyMaps", "called dropmarker() method from network");

            //relaunch request for network location updates (not needed since already requested in previous code)
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            dotColor = false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a log.d and/or toast
            Log.d("MyMaps", "Network onStatusChanged called");
            Toast.makeText(MapsActivity.this, "Network onStatusChanged called", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void dropMarker(String provider) {

        LatLng userLocation = null;

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

        if (myLocation == null) {
            //display a log d message and/or toast
            Log.d("MyMaps", "dropMarker: myLocation is null");

        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display log d message and/or toast of coordinates
            Toast.makeText(MapsActivity.this, "" + myLocation.getLatitude() + ", " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            Circle myCircle;

            //add a shape for a marker (don't use standard teardrop marker)
            //dotColor determines whether device is using network or GPS and changes the color accordingly

            if (dotColor == true) {
                myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.MAGENTA));
                Log.d("MyMaps", "Red dot laid for GPS");
            } else if (dotColor == false) {
                myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.GREEN).strokeWidth(2).fillColor(Color.BLUE));
                Log.d("MyMaps", "Blue dot laid for network");
            }




            mMap.animateCamera(update);
        }

    }

    public void clearOverlays(View v) {
        mMap.clear();
    }




}