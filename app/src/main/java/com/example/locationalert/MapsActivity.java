/*
// Mobile Computing CMSC 628
// Assignment 2
// Team Members - Rujuta Palande, Kshipra Kode
// Application Name - Location Bump
*/
package com.example.locationalert;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {

    public static final String MyPREFERENCES = "LocationBump";
    public static int distanceBetween = 200;
    private GoogleMap mMap;
    private Location finalAddress;
    private EditText locationName;
    private Marker m, destination;
    private String destinationLongi, destinationLati, destinationName;
    private String DLongi = "DestinationName", DLati = "DestinationLati", DName = "DestinationLongi";
    Circle circle;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    LatLngBounds.Builder b;
    LatLngBounds bounds;
    boolean firsttime = false;
    Handler myHandler;
    String color = "#03A9F4";
    String fill = "#B3B3F5FC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        myHandler = new Handler();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Setting up Shared Preferences to save the last location and last search option
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        destinationLongi = sp.getString(DLongi, null);
        destinationName = sp.getString(DName, null);
        destinationLati = sp.getString(DLati, null);

        //Initializing the View elements like the Buttons and the TextViews
        ImageButton addButton = (ImageButton) findViewById(R.id.buttonAddLocation);
        locationName = (EditText) findViewById(R.id.location);

        //Activating the Listener on the Add Destination button
        addButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Using location Manager to get current location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Check if the user has location permissions enabled for accessing the location
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Setting the time after which the Android System should check the location - We have set it to 10 ms
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, Criteria.ACCURACY_FINE, this);
            mMap.setMyLocationEnabled(true);

            if (destinationName != null && destinationLati != null && destinationLongi != null
                    && !destinationLati.isEmpty() && !destinationLongi.isEmpty() && !destinationName.isEmpty()) {
                finalAddress = new Location("Destination");
                finalAddress.setLatitude(Double.valueOf(destinationLati));
                finalAddress.setLongitude(Double.valueOf(destinationLongi));

                addComponents();
                locationName.setText(destinationName);
                UpdateCamera();
            }
        }
    }

    public void onClick(View v) {

        //If some location entered in the textbox
        if (!locationName.getText().toString().equals("")) {

            //Use the Geocoder class to get the longitude and latitude of the location
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> gotAddresses = null;
            try {
                gotAddresses = geocoder.getFromLocationName(locationName.getText().toString(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (gotAddresses != null) {
                    if (gotAddresses.size() <= 0) {
                        Toast.makeText(MapsActivity.this, "Please Enter a valid Destination", Toast.LENGTH_SHORT).show();
                        if (destination != null)
                            destination.remove();

                        UpdateEditor(null, null, null);

                    } else {

                        Address received = gotAddresses.get(0);
                        //Remove previous markers if any
                        if (destination != null)
                            destination.remove();
                        if (circle != null)
                            circle.remove();

                        destinationName = locationName.getText().toString();
                        finalAddress.setLatitude(received.getLatitude());
                        finalAddress.setLongitude(received.getLongitude());

                        addComponents();
                        UpdateCamera();
                        UpdateEditor(locationName.getText().toString(), String.valueOf(received.getLongitude()), String.valueOf(received.getLatitude()));

                        Toast.makeText(MapsActivity.this, "Destination Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Adds the marker and the circle to the graph
    void addComponents() {

        destination = mMap.addMarker(new MarkerOptions().
                position(new LatLng(finalAddress.getLatitude(), finalAddress.getLongitude())).
                title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        circle = mMap.addCircle(new CircleOptions().radius(distanceBetween).strokeColor(Color.parseColor(color)).zIndex(-1).fillColor(Color.parseColor(fill)).strokeWidth(3).center(new LatLng(finalAddress.getLatitude(), finalAddress.getLongitude())));

    }

    //Updates the Camera Zoom level
    void UpdateCamera() {

        b = new LatLngBounds.Builder();
        b.include(destination.getPosition());
        if (m != null)
            b.include(m.getPosition());
        bounds = b.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));

    }

    //Updates the changes to the shared preferences
    public void UpdateEditor(String Name, String Longitude, String Latitude) {
        editor = sp.edit();
        editor.putString(DName, Name);
        editor.putString(DLongi, Longitude);
        editor.putString(DLati, Latitude);
        editor.commit();
    }

    @Override
    public void onLocationChanged(Location location) {

        myHandler.post(new MyRunnable(location));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Status", "Accurate Location Displayed");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Status", "Last Known Location Displayed");
    }


    private class MyRunnable implements Runnable {
        Location _location;

        public MyRunnable(Location loc) {
            _location = loc;
        }

        @Override
        public void run() {

            //Remove any previous marker set
            if (m != null)
                m.remove();

            //Add new marker with respect to the updated location
            m = mMap.addMarker(new MarkerOptions().position(new LatLng(_location.getLatitude(), _location.getLongitude())).title("Current Location"));

            String textDislplay = destinationName;

            locationName.setText(textDislplay);
            //Check the distance between the final destination and the current location and display a toast when the distance is less than 200 meters
            if (finalAddress != null) {
                if (_location.distanceTo(finalAddress) < distanceBetween) {
                    Toast.makeText(MapsActivity.this, "You are " + _location.distanceTo(finalAddress) + "from the Destination", Toast.LENGTH_SHORT).show();
                }
            }

            if (!firsttime) {
                b = new LatLngBounds.Builder();
                if (destination != null)
                    b.include(destination.getPosition());
                if (m != null)
                    b.include(m.getPosition());
                bounds = b.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));
                firsttime = true;
            }
        }
    }

}
