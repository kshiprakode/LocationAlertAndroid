package com.example.locationalert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {

    private GoogleMap mMap;
    double cLongitude, cLatitude;

    Location location, finalAddress;
    private EditText locationName;
    private TextView FinalAddress;
    LocationManager locationManager;
    Marker m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing the View elements like the Buttons and the TextViews
        Button addButton = (Button) findViewById(R.id.buttonAddLocation);
        locationName = (EditText) findViewById(R.id.location);
        FinalAddress = (TextView) findViewById(R.id.finalLocation);

        //Activating the Listener on the Add Destination button
        addButton.setOnClickListener(this);
    }

    @Override

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Using location Manager to get current location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Check if the user has location permissions enabled for accessing the location

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, Criteria.ACCURACY_FINE, this);
            mMap.setMyLocationEnabled(true);
        }
    }

    public void onClick(View v) {

        if (!locationName.getText().toString().equals("")) {

            //Use the
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> gotAddresses = null;
            try {
                gotAddresses = geocoder.getFromLocationName(locationName.getText().toString() + " near me", 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Address received = gotAddresses.get(0);

                mMap.addMarker(new MarkerOptions().position(new LatLng(received.getLatitude(), received.getLongitude())).title(locationName.toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(received.getLatitude(), received.getLongitude())));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));

                finalAddress = new Location("Final Address");
                finalAddress.setLatitude(received.getLatitude());
                finalAddress.setLongitude(received.getLongitude());

                View text1 = findViewById(R.id.location);
                View text2 = findViewById(R.id.finalLocation);
                text1.setVisibility(View.INVISIBLE);
                v.setVisibility(View.INVISIBLE);
                text2.setVisibility(View.VISIBLE);

                FinalAddress.setText(received.getAddressLine(0));
                Toast.makeText(MapsActivity.this, "You are " + location.distanceTo(finalAddress) + "from the Destination", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(MapsActivity.this, "Please specify destination address", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

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
        m = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        if (finalAddress != null) {
            if (location.distanceTo(finalAddress) < 200) {
                Toast.makeText(MapsActivity.this, "You are " + location.distanceTo(finalAddress) + "from the Destination", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
