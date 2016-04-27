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
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {

    public static final String MyPREFERENCES = "LocationBump" ;
    private GoogleMap mMap;
    private Location finalAddress;
    private EditText locationName;
    private Marker m,destination;
    private String destinationLongi,destinationLati,destinationName;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    LatLngBounds.Builder b;
    LatLngBounds bounds;
    boolean firsttime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Setting up Shared Preferences to save the last location and last search option
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sp.edit();
        destinationLongi = sp.getString("DestinationLongi", null);
        destinationName = sp.getString("DestinationName", null);
        destinationLati = sp.getString("DestinationLati", null);
        editor.apply();

        //Checking if your shared preferences have the last saved destination
        finalAddress = new Location("Final Address");

        if(destinationLati != null && destinationLongi != null) {
            Log.d("Set Address", destinationLati + destinationLongi);
            finalAddress.setLongitude(Double.parseDouble(destinationLongi));
            finalAddress.setLatitude(Double.parseDouble(destinationLati));
        }

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
        }


        if(destination!=null)
            destination.remove();
        destination = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(destinationLati),Double.parseDouble(destinationLongi))).title(locationName.toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(destinationName));
        locationName.setText(destinationName);

        b = new LatLngBounds.Builder();
        b.include(destination.getPosition());
        if(m!=null)
            b.include(m.getPosition());
        bounds = b.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));

    }

    public void onClick(View v) {

        //If some location entered in the textbox
        if (!locationName.getText().toString().equals("")) {

            //Use the geocoder class to get the longitude and latitude of the location
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> gotAddresses = null;
            try {
                gotAddresses = geocoder.getFromLocationName(locationName.getText().toString(), 1);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try{
                if (gotAddresses != null) {
                    if(gotAddresses.size()<=0)
                    {
                        Toast.makeText(MapsActivity.this, "Please Enter a valid Destination", Toast.LENGTH_SHORT).show();
                        if(destination!=null)
                            destination.remove();
                        editor.putString("DestinationName",null);
                        editor.putString("DestinationsLongi",null);
                        editor.putString("DestinationLati", null);

                    }
                    else {

                        Address received = gotAddresses.get(0);
                        Log.d("Destination", gotAddresses.get(0).toString());
                        if(destination!=null)
                            destination.remove();
                        destinationName = locationName.getText().toString();
                        destination = mMap.addMarker(new MarkerOptions().position(new LatLng(received.getLatitude(), received.getLongitude())).title(locationName.toString()).title(locationName.toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(destinationName));
                        b.include(destination.getPosition());
                        bounds = b.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));

                        finalAddress.setLatitude(received.getLatitude());
                        finalAddress.setLongitude(received.getLongitude());
                        editor.putString("DestinationName",locationName.getText().toString());
                        editor.putString("DestinationsLongi",String.valueOf(finalAddress.getLongitude()));
                        editor.putString("DestinationLati",String.valueOf(finalAddress.getLatitude()));
                        editor.commit();

                        Toast.makeText(MapsActivity.this, "Destination Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (Exception e)
            {
                Log.d("ERROR","Address not found");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        //Remove any previous marker set
        if(m!=null)
            m.remove();


        //Add new marker with respect to the updated location
        m = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current Location"));

        String textDislplay = destinationName + " : " + location.distanceTo(finalAddress);
        locationName.setText(textDislplay);
        //Check the distance between the final destination and the current location and display a toast when the distance is less than 200 meters
        if (finalAddress != null) {
            if (location.distanceTo(finalAddress) < 200) {
                Toast.makeText(MapsActivity.this, "You are " + location.distanceTo(finalAddress) + "from the Destination", Toast.LENGTH_SHORT).show();
            }
        }
        if(!firsttime) {
            b = new LatLngBounds.Builder();
            b.include(destination.getPosition());
            if (m != null)
                b.include(m.getPosition());
            bounds = b.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));
            firsttime= true;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Status" , "Accurate Location Displayed");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Status", "Last Known Location Displayed");

    }
}
